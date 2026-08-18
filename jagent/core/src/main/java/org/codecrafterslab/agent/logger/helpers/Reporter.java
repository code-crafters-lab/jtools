package org.codecrafterslab.agent.logger.helpers;

/**
 * 错误报告工具类
 *
 * <p>用于向标准错误流输出异常报告，记录日志框架内部
 * 处理过程中出现的非致命错误信息
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time 2020/04/30 15:12
 */
public class Reporter {
    /**
     * 输出错误信息及异常堆栈到标准错误流
     *
     * @param msg 错误描述信息
     * @param t   异常对象
     */
    public static void error(String msg, Throwable t) {
        System.err.println(msg);
        System.err.println("Reported exception:");
        t.printStackTrace();
    }
}
