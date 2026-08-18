package org.codecrafterslab.agent.logger.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 文件日志写入任务
 *
 * <p>将格式化后的日志内容写入按日期命名的日志文件（如
 * {@code yyyyMMdd.log}），继承自 {@link ConsoleWriteTask}，
 * 将输出流切换到对应的日志文件进行追加写入
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time 2020/04/29 13:58
 */
public class FileWriteTask extends ConsoleWriteTask {

    /** 日志文件按日期命名使用的日期格式 */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 日志文件所在目录 */
    private final Path logDirPath;

    /**
     * 构造文件写入任务
     *
     * @param logDir     日志文件所在的目录
     * @param outContent 日志内容
     * @param exception 关联异常，可为 null
     */
    public FileWriteTask(Path logDir, String outContent, Throwable exception) {
        super(outContent, exception);
        this.logDirPath = logDir;
    }

    @Override
    public void run() {
        if (logDirPath == null) return;
        File logFile = new File(logDirPath.toFile(), String.format("%s.log", LocalDateTime.now().format(DATE_TIME_FORMATTER)));
        try {
            if (!Files.exists(logFile.toPath())) {
                Files.createFile(logFile.toPath());
            }
        } catch (Exception e) {
            writeException("failed to create log file!", e, System.err);
        }

        try (PrintStream ps = new PrintStream(new FileOutputStream(logFile, true))) {
            setPrintStream(ps);
            super.run();
        } catch (FileNotFoundException e) {
            writeException("log file not found!", e, System.err);
        }
    }
}
