package org.codecrafterslab.agent.logger;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 日志级别枚举
 *
 * <p>定义日志的级别体系及其数值大小关系，数值越小级别越明细，
 * 用于日志开关判断（如 INFO 级别可输出其下所有明细级别日志）
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time 2020/04/29 14:13
 */
@Getter
@AllArgsConstructor
public enum Level {
    /** 全部级别 */
    ALL(Integer.MIN_VALUE, "ALL"),
    /** 追踪级别 */
    TRACE(0, "TRACE"),
    /** 调试级别 */
    DEBUG(100, "DEBUG"),
    /** 信息级别 */
    INFO(200, "INFO"),
    /** 警告级别 */
    WARN(300, "WARN"),
    /** 错误级别 */
    ERROR(400, "ERROR"),
    /** 关闭（不输出任何日志） */
    OFF(Integer.MAX_VALUE, "OFF");

    /** 级别的数值表示，用于大小比较 */
    private final int levelInt;
    /** 级别的字符串表示 */
    private final String levelStr;

    /**
     * 判断当前级别是否不高于指定级别
     *
     * @param level 待比较的日志级别
     * @return 当前级别数值小于等于指定级别时返回 true
     */
    public boolean isLesserOrEqual(Level level) {
        return this.levelInt <= level.levelInt;
    }

    /**
     * 按字符串名称解析日志级别
     *
     * <p>名称匹配忽略大小写，无法识别的字符串或空值返回默认级别
     *
     * @param sArg         日志级别字符串
     * @param defaultLevel  默认级别，解析失败时返回
     * @return 解析得到的日志级别
     */
    public static Level toLevel(String sArg, Level defaultLevel) {
        if (sArg == null || sArg.isEmpty()) {
            return defaultLevel;
        } else if ("ALL".equalsIgnoreCase(sArg)) {
            return ALL;
        } else if ("TRACE".equalsIgnoreCase(sArg)) {
            return TRACE;
        } else if ("DEBUG".equalsIgnoreCase(sArg)) {
            return DEBUG;
        } else if ("INFO".equalsIgnoreCase(sArg)) {
            return INFO;
        } else if ("WARN".equalsIgnoreCase(sArg)) {
            return WARN;
        } else if ("ERROR".equalsIgnoreCase(sArg)) {
            return ERROR;
        } else {
            return "OFF".equalsIgnoreCase(sArg) ? OFF : defaultLevel;
        }
    }

    @Override
    public String toString() {
        return this.levelStr;
    }

}
