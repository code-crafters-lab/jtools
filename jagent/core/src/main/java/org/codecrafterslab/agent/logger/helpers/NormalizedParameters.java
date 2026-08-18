package org.codecrafterslab.agent.logger.helpers;

import lombok.Getter;

/**
 * 标准化后的日志参数对象
 *
 * <p>将日志消息、参数数组与异常统一封装，用于日志格式化过程，
 * 提供从参数数组中识别异常及去除尾部异常的辅助能力
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time 2020/04/30 15:12
 */
@Getter
public class NormalizedParameters {
    /** 日志消息模板 */
    final String message;
    /** 参与格式化的参数数组 */
    final Object[] arguments;
    /** 日志关联的异常对象 */
    final Throwable throwable;

    /**
     * 构造标准化参数
     *
     * @param message   日志消息模板
     * @param arguments 参数数组
     * @param throwable 关联异常，可为 null
     */
    public NormalizedParameters(String message, Object[] arguments, Throwable throwable) {
        this.message = message;
        this.arguments = arguments;
        this.throwable = throwable;
    }

    /**
     * 构造无异常的标准化参数
     *
     * @param message   日志消息模板
     * @param arguments 参数数组
     */
    public NormalizedParameters(String message, Object[] arguments) {
        this(message, arguments, null);
    }

    /**
     * 从参数数组中识别尾部的异常对象
     *
     * <p>仅当数组非空且最后一个元素为异常类型时返回该异常
     *
     * @param argArray 参数数组
     * @return 尾部的异常对象，未识别到时返回 null
     */
    public static Throwable getThrowableCandidate(final Object[] argArray) {
        if (argArray == null || argArray.length == 0) {
            return null;
        }

        final Object lastEntry = argArray[argArray.length - 1];
        if (lastEntry instanceof Throwable) {
            return (Throwable) lastEntry;
        }

        return null;
    }

    /**
     * 返回去除尾部异常对象后的参数数组副本
     *
     * @param argArray 原始参数数组，必须非空
     * @return 去除最后一个元素后的新数组
     */
    public static Object[] trimmedCopy(final Object[] argArray) {
        if (argArray == null || argArray.length == 0) {
            throw new IllegalStateException("non-sensical empty or null argument array");
        }

        final int trimmedLen = argArray.length - 1;

        Object[] trimmed = new Object[trimmedLen];

        if (trimmedLen > 0) {
            System.arraycopy(argArray, 0, trimmed, 0, trimmedLen);
        }

        return trimmed;
    }

    /**
     * 标准化日志参数
     *
     * <p>当显式指定异常或参数尾部为异常对象时，将该异常从参数中剥离
     * 并单独保存，便于格式化与异常输出的分离
     *
     * @param msg       日志消息模板
     * @param arguments 参数数组
     * @param t        显式指定的异常，可为 null
     * @return 标准化后的参数对象
     */
    public static NormalizedParameters normalize(String msg, Object[] arguments, Throwable t) {

        if (t != null) {
            return new NormalizedParameters(msg, arguments, t);
        }

        if (arguments == null || arguments.length == 0) {
            return new NormalizedParameters(msg, arguments, t);
        }

        Throwable throwableCandidate = NormalizedParameters.getThrowableCandidate(arguments);
        if (throwableCandidate != null) {
            Object[] trimmedArguments = MessageFormatter.trimmedCopy(arguments);
            return new NormalizedParameters(msg, trimmedArguments, throwableCandidate);
        } else {
            return new NormalizedParameters(msg, arguments);
        }

    }

//    public static NormalizedParameters normalize(LoggingEvent event) {
//        return normalize(event.getMessage(), event.getArgumentArray(), event.getThrowable());
//    }
}
