package org.codecrafterslab.agent.logger.impl;

import java.io.PrintStream;

/**
 * 控制台日志写入任务
 *
 * <p>将格式化后的日志内容及可能的异常写入指定的输出流。
 * 根据是否存在异常自动选择标准输出或标准错误流，异常日志走错误流
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time 2020/04/29 13:58
 */
public class ConsoleWriteTask implements Runnable {
    /** 日志内容 */
    private final String outContent;
    /** 关联异常对象 */
    private final Throwable exception;
    /** 输出流，缺省指向标准输出或标准错误流 */
    private PrintStream ps;

    /**
     * 构造控制台写入任务
     *
     * @param outContent 日志内容
     * @param exception  关联异常，有异常时输出到错误流
     */
    ConsoleWriteTask(String outContent, Throwable exception) {
        this.outContent = outContent;
        this.exception = exception;
        setPrintStream(null == exception ? System.out : System.err);
    }

    /**
     * 写入普通日志内容
     *
     * @param content 日志内容
     * @param ps     输出流
     */
    protected void writeContent(String content, PrintStream ps) {
        if (null == ps) return;
        ps.print(content);
    }

    /**
     * 写入日志内容与异常堆栈
     *
     * @param content 日志内容
     * @param e      异常对象
     * @param ps     输出流
     */
    protected void writeException(String content, Throwable e, PrintStream ps) {
        if (null == ps) return;
        ps.print(content);
        e.printStackTrace(ps);
    }

    /**
     * 统一写入入口，依据是否存在异常选择内容或异常写入
     *
     * @param content 日志内容
     * @param e      异常对象，可为 null
     * @param stream 输出流
     */
    protected void write(String content, Throwable e, PrintStream stream) {
        if (null == e) {
            writeContent(content, stream);
            return;
        }
        writeException(content, e, stream);
    }

    /**
     * 获取当前输出流
     *
     * @return 输出流
     */
    protected PrintStream getPrintStream() {
        return ps;
    }

    /**
     * 设置输出流
     *
     * @param ps 输出流
     */
    protected void setPrintStream(PrintStream ps) {
        this.ps = ps;
    }

    @Override
    public void run() {
        write(outContent, exception, getPrintStream());
    }
}
