package org.codecrafterslab.agent.logger;

import lombok.Getter;

/**
 * 日志消息格式化结果对象
 *
 * <p>承载一次日志消息格式化后的完整信息，包含格式化后的消息文本、
 * 原始参数数组及关联的异常信息，供日志输出时使用
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time 2020/04/30 14:50
 */
@Getter
public class FormattingTuple {
    /** 格式化完成后的日志消息文本 */
    private final String message;
    /** 参与格式化的原始参数数组 */
    private final Object[] argArray;
    /** 日志关联的异常对象，无异常时为 null */
    private final Throwable throwable;

    /**
     * 构造格式化结果
     *
     * @param message   格式化后的消息文本
     * @param argArray  原始参数数组
     * @param throwable 关联异常，可为 null
     */
    public FormattingTuple(String message, Object[] argArray, Throwable throwable) {
        this.message = message;
        this.argArray = argArray;
        this.throwable = throwable;
    }

    /**
     * 构造仅包含消息文本的格式化结果
     *
     * @param message 格式化后的消息文本
     */
    public FormattingTuple(String message) {
        this(message, null, null);
    }

}
