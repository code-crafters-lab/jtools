package org.codecrafterslab.agent.logger.impl;

import org.codecrafterslab.agent.logger.FormattingTuple;
import org.codecrafterslab.agent.logger.Level;
import org.codecrafterslab.agent.logger.Logger;
import org.codecrafterslab.agent.logger.Output;
import org.codecrafterslab.agent.logger.helpers.MessageFormatter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 日志记录器实现
 *
 * <p>实现 {@link Logger} 接口，根据配置的日志级别决定各级别日志是否输出，
 * 并通过独立的线程池将日志任务分发到控制台或文件。
 * 相关配置通过系统属性提供：
 * <ul>
 *   <li>{@code ccl.agent.log.level}：日志级别，默认关闭</li>
 *   <li>{@code ccl.agent.log.dir}：日志目录，默认 {@code logs/jagent}</li>
 *   <li>{@code ccl.agent.log.output}：输出目标（console、file，可逗号分隔）</li>
 * </ul>
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time 2020/04/29 13:58
 */
public class InnerLogger implements Logger {
    /**
     * 线程池队列容量，防止任务无限堆积导致内存溢出
     */
    private static final int QUEUE_CAPACITY;
    /**
     * 控制台输出线程池
     */
    private static final ExecutorService CONSOLE_EXECUTOR;
    /**
     * 文件输出线程池
     */
    private static final ExecutorService FILE_EXECUTOR;
    /**
     * 全局日志级别
     */
    private static final Level LOG_LEVEL;
    /**
     * 日志输出目录
     */
    private static final Path LOG_DIR_PATH;
    /**
     * 日志输出目标列表
     */
    private static final List<Output> LOG_OUTPUT;
    /**
     * 日志时间戳格式化器
     */
    private static final DateTimeFormatter FORMATTER;

    /**
     * 日志级别配置
     */
    private final Level level;
    /**
     * 日志记录器名称
     */
    private final String name;
    /**
     * 日志输出目录
     */
    private final Path logDirPath;

    static {
        QUEUE_CAPACITY = 1024;
        CONSOLE_EXECUTOR = newBoundedSingleThreadExecutor("ccl-agent-console");
        FILE_EXECUTOR = newBoundedSingleThreadExecutor("ccl-agent-file");
        LOG_LEVEL = Level.toLevel(System.getProperty("ccl.agent.log.level", ""), Level.INFO);
        LOG_DIR_PATH = Paths.get(System.getProperty("ccl.agent.log.dir", "logs/jagent"));
        LOG_OUTPUT = Output.from(System.getProperty("ccl.agent.log.output", "file"));
        FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    }

    /**
     * 构造日志记录器
     *
     * @param name 日志记录器名称
     */
    public InnerLogger(String name) {
        this.name = name;
        this.level = LOG_LEVEL;
        this.logDirPath = LOG_DIR_PATH;
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isTraceEnabled() {
        return this.level.isLesserOrEqual(Level.TRACE);
    }

    @Override
    public void trace(String format, Object... args) {
        if (isTraceEnabled()) {
            logger(Level.TRACE, format, args);
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return this.level.isLesserOrEqual(Level.DEBUG);
    }

    @Override
    public void debug(String format, Object... args) {
        if (isDebugEnabled()) {
            logger(Level.DEBUG, format, args);
        }
    }

    @Override
    public boolean isInfoEnabled() {
        return this.level.isLesserOrEqual(Level.INFO);
    }

    @Override
    public void info(String format, Object... args) {
        if (isInfoEnabled()) {
            logger(Level.INFO, format, args);
        }
    }

    @Override
    public boolean isWarnEnabled() {
        return this.level.isLesserOrEqual(Level.WARN);
    }

    @Override
    public void warn(String format, Object... args) {
        if (isWarnEnabled()) {
            logger(Level.WARN, format, args);
        }
    }

    @Override
    public boolean isErrorEnabled() {
        return this.level.isLesserOrEqual(Level.ERROR);
    }

    @Override
    public void error(String format, Object... args) {
        if (isErrorEnabled()) {
            logger(Level.ERROR, format, args);
        }
    }

    /**
     * 日志输出
     *
     * @param format 格式化字符串
     * @param args   参数
     */
    private void logger(Level level, String format, Object... args) {
        FormattingTuple formattingTuple = MessageFormatter.arrayFormat(format, args);
        String out = String.format("%s [%-5s] %s: %s%n", LocalDateTime.now().format(FORMATTER),
                level.getLevelStr(), name, formattingTuple.getMessage());

        if (LOG_OUTPUT.contains(Output.CONSOLE)) {
            CONSOLE_EXECUTOR.execute(new ConsoleWriteTask(out, formattingTuple.getThrowable()));
        }

        if (LOG_OUTPUT.contains(Output.FILE)) {
            FILE_EXECUTOR.execute(new FileWriteTask(logDirPath, out, formattingTuple.getThrowable()));
        }
    }

    static {
        try {
            Files.createDirectories(LOG_DIR_PATH);
        } catch (IOException e) {
            System.err.println("failed to create log directory: " + LOG_DIR_PATH);
            e.printStackTrace(System.err);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(InnerLogger::shutdown, "ccl-agent-shutdown"));
    }

    /**
     * 创建具有有界队列的单线程线程池
     *
     * <p>队列满时丢弃新任务（DiscardPolicy），避免高并发日志导致内存溢出；
     * 线程标记为守护线程，避免阻塞 JVM 优雅退出
     *
     * @param threadName 线程名称
     * @return 单线程线程池
     */
    private static ExecutorService newBoundedSingleThreadExecutor(String threadName) {
        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                r -> {
                    Thread t = new Thread(r, threadName);
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.DiscardPolicy());
    }

    /**
     * 关闭所有线程池并等待待处理日志排空
     */
    private static void shutdown() {
        FILE_EXECUTOR.shutdown();
        CONSOLE_EXECUTOR.shutdown();
        try {
            FILE_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS);
            CONSOLE_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
