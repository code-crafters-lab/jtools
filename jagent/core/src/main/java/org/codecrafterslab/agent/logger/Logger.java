package org.codecrafterslab.agent.logger;

/**
 * 日志记录器接口
 *
 * <p>定义统一的日志操作入口，按日志级别（TRACE、DEBUG、INFO、WARN、ERROR）
 * 提供对应的启用状态判断与消息输出方法，实现方负责具体的格式化和输出逻辑
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time 2019/06/27 08:51
 */
public interface Logger {
    /**
     * 日志名称
     *
     * @return String 日志记录器名称
     */
    String getName();

    /**
     * 是否启用追踪级别日志
     *
     * @return boolean 启用返回 true，否则返回 false
     */
    boolean isTraceEnabled();

    /**
     * 追踪级别日志输出
     *
     * @param format 格式化字符串
     * @param args   参数
     */
    void trace(String format, Object... args);

    /**
     * 是否启用调试级别日志
     *
     * @return boolean 启用返回 true，否则返回 false
     */
    boolean isDebugEnabled();

    /**
     * 调试日志输出
     *
     * @param format 格式化字符串
     * @param args   参数
     */
    void debug(String format, Object... args);

    /**
     * 是否启用信息级别日志
     *
     * @return boolean 启用返回 true，否则返回 false
     */
    boolean isInfoEnabled();

    /**
     * 信息日志输出
     *
     * @param format 格式化字符串
     * @param args   参数
     */
    void info(String format, Object... args);

    /**
     * 是否启用警告级别日志
     *
     * @return boolean 启用返回 true，否则返回 false
     */
    boolean isWarnEnabled();

    /**
     * 警告日志输出
     *
     * @param format 格式化字符串
     * @param args   参数
     */
    void warn(String format, Object... args);

    /**
     * 是否启用错误级别日志
     *
     * @return boolean 启用返回 true，否则返回 false
     */
    boolean isErrorEnabled();

    /**
     * 错误日志输出
     *
     * @param format 格式化字符串
     * @param args   参数
     */
    void error(String format, Object... args);

}
