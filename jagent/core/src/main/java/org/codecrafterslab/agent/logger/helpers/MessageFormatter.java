package org.codecrafterslab.agent.logger.helpers;

import org.codecrafterslab.agent.logger.FormattingTuple;

import java.util.HashMap;
import java.util.Map;

/**
 * 日志消息格式化工具类
 *
 * <p>负责将含占位符 {@code {}} 的消息模板与参数进行占位符替换，
 * 支持转义、嵌套数组展开以及尾部异常参数的识别与剥离，
 * 为避免不必要的对象创建，本类为不可实例化的工具类
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time 2020/04/30 14:48
 */
public final class MessageFormatter {
    /** 占位符起始字符 */
    static final char DELIM_START = '{';
    /** 占位符结束字符 */
    static final char DELIM_STOP = '}';
    /** 占位符字符串 */
    static final String DELIM_STR = "{}";
    /** 转义字符 */
    private static final char ESCAPE_CHAR = '\\';

    /**
     * 按单个参数格式化消息
     *
     * @param messagePattern 消息模板
     * @param arg           参数
     * @return 格式化结果
     */
    public static FormattingTuple format(String messagePattern, Object arg) {
        return arrayFormat(messagePattern, new Object[]{arg});
    }

    /**
     * 按两个参数格式化消息
     *
     * @param messagePattern 消息模板
     * @param arg1 第一个参数
     * @param arg2 第二个参数
     * @return 格式化结果
     */
    public static FormattingTuple format(final String messagePattern, Object arg1, Object arg2) {
        return arrayFormat(messagePattern, new Object[]{arg1, arg2});
    }

    /**
     * 按参数数组格式化消息，并自动识别剥离尾部的异常参数
     *
     * @param messagePattern 消息模板
     * @param argArray      参数数组
     * @return 格式化结果
     */
    public static FormattingTuple arrayFormat(final String messagePattern, final Object[] argArray) {
        Throwable throwableCandidate = MessageFormatter.getThrowableCandidate(argArray);
        Object[] args = argArray;
        if (throwableCandidate != null) {
            args = MessageFormatter.trimmedCopy(argArray);
        }
        return arrayFormat(messagePattern, args, throwableCandidate);
    }

    /**
     * 按参数数组执行基础格式化并仅返回消息文本
     *
     * @param messagePattern 消息模板
     * @param argArray      参数数组
     * @return 格式化后的消息文本
     */
    public static String basicArrayFormat(final String messagePattern, final Object[] argArray) {
        FormattingTuple ft = arrayFormat(messagePattern, argArray, null);
        return ft.getMessage();
    }

    /**
     * 基于标准化参数执行基础格式化并仅返回消息文本
     *
     * @param np 标准化后的日志参数
     * @return 格式化后的消息文本
     */
    public static String basicArrayFormat(NormalizedParameters np) {
        return basicArrayFormat(np.getMessage(), np.getArguments());
    }

    /**
     * 按参数数组和指定异常执行格式化
     *
     * @param messagePattern 消息模板
     * @param argArray      参数数组
     * @param throwable     关联异常
     * @return 格式化结果
     */
    public static FormattingTuple arrayFormat(final String messagePattern, final Object[] argArray, Throwable throwable) {

        if (messagePattern == null) {
            return new FormattingTuple(null, argArray, throwable);
        }

        if (argArray == null) {
            return new FormattingTuple(messagePattern);
        }

        int i = 0;
        int j;
        // use string builder for better multicore performance
        StringBuilder sbuf = new StringBuilder(messagePattern.length() + 50);

        int L;
        for (L = 0; L < argArray.length; L++) {

            j = messagePattern.indexOf(DELIM_STR, i);

            if (j == -1) {
                // no more variables
                if (i == 0) { // this is a simple string
                    return new FormattingTuple(messagePattern, argArray, throwable);
                } else { // add the tail string which contains no variables and return
                    // the result.
                    sbuf.append(messagePattern, i, messagePattern.length());
                    return new FormattingTuple(sbuf.toString(), argArray, throwable);
                }
            } else {
                if (isEscapedDelimiter(messagePattern, j)) {
                    if (!isDoubleEscaped(messagePattern, j)) {
                        L--; // DELIM_START was escaped, thus should not be incremented
                        sbuf.append(messagePattern, i, j - 1);
                        sbuf.append(DELIM_START);
                        i = j + 1;
                    } else {
                        // The escape character preceding the delimiter start is
                        // itself escaped: "abc x:\\{}"
                        // we have to consume one backward slash
                        sbuf.append(messagePattern, i, j - 1);
                        deeplyAppendParameter(sbuf, argArray[L], new HashMap<>());
                        i = j + 2;
                    }
                } else {
                    // normal case
                    sbuf.append(messagePattern, i, j);
                    deeplyAppendParameter(sbuf, argArray[L], new HashMap<>());
                    i = j + 2;
                }
            }
        }
        // append the characters following the last {} pair.
        sbuf.append(messagePattern, i, messagePattern.length());
        return new FormattingTuple(sbuf.toString(), argArray, throwable);
    }

    /**
     * 判断占位符起始位置前是否带有转义字符
     *
     * @param messagePattern     消息模板
     * @param delimiterStartIndex 占位符起始索引
     * @return 前一个字符为转义字符时返回 true
     */
    static boolean isEscapedDelimiter(String messagePattern, int delimiterStartIndex) {
        if (delimiterStartIndex == 0) return false;
        char potentialEscape = messagePattern.charAt(delimiterStartIndex - 1);
        return potentialEscape == ESCAPE_CHAR;
    }

    /**
     * 判断占位符起始位置前两个字符是否为转义字符（双重转义）
     *
     * @param messagePattern     消息模板
     * @param delimiterStartIndex 占位符起始索引
     * @return 前两个字符为转义字符时返回 true
     */
    static boolean isDoubleEscaped(String messagePattern, int delimiterStartIndex) {
        return delimiterStartIndex >= 2 && messagePattern.charAt(delimiterStartIndex - 2) == ESCAPE_CHAR;
    }

    // special treatment of array values was suggested by 'lizongbo'
    private static void deeplyAppendParameter(StringBuilder buff, Object o, Map<Object[], Object> seenMap) {
        if (o == null) {
            buff.append("null");
            return;
        }
        if (!o.getClass().isArray()) {
            safeObjectAppend(buff, o);
        } else {
            // check for primitive array types because they
            // unfortunately cannot be cast to Object[]
            if (o instanceof boolean[]) {
                booleanArrayAppend(buff, (boolean[]) o);
            } else if (o instanceof byte[]) {
                byteArrayAppend(buff, (byte[]) o);
            } else if (o instanceof char[]) {
                charArrayAppend(buff, (char[]) o);
            } else if (o instanceof short[]) {
                shortArrayAppend(buff, (short[]) o);
            } else if (o instanceof int[]) {
                intArrayAppend(buff, (int[]) o);
            } else if (o instanceof long[]) {
                longArrayAppend(buff, (long[]) o);
            } else if (o instanceof float[]) {
                floatArrayAppend(buff, (float[]) o);
            } else if (o instanceof double[]) {
                doubleArrayAppend(buff, (double[]) o);
            } else {
                objectArrayAppend(buff, (Object[]) o, seenMap);
            }
        }
    }

    /**
     * 安全地追加对象的字符串表示，toString 异常时记录错误并输出占位信息
     *
     * @param buff 字符缓冲区
     * @param o    目标对象
     */
    private static void safeObjectAppend(StringBuilder buff, Object o) {
        try {
            String oAsString = o.toString();
            buff.append(oAsString);
        } catch (Throwable t) {
            Reporter.error("Failed toString() invocation on an object of type [" + o.getClass().getName() + "]", t);
            buff.append("[FAILED toString()]");
        }

    }

    /**
     * 追加对象数组参数，自动处理嵌套数组并防止循环引用
     *
     * @param buff    字符缓冲区
     * @param a       对象数组
     * @param seenMap 已访问数组记录，用于检测循环引用
     */
    private static void objectArrayAppend(StringBuilder buff, Object[] a, Map<Object[], Object> seenMap) {
        buff.append('[');
        if (!seenMap.containsKey(a)) {
            seenMap.put(a, null);
            final int len = a.length;
            for (int i = 0; i < len; i++) {
                deeplyAppendParameter(buff, a[i], seenMap);
                if (i != len - 1)
                    buff.append(", ");
            }
            // allow repeats in siblings
            seenMap.remove(a);
        } else {
            buff.append("...");
        }
        buff.append(']');
    }

    /**
     * 追加布尔数组参数
     *
     * @param buff 字符缓冲区
     * @param a    布尔数组
     */
    private static void booleanArrayAppend(StringBuilder buff, boolean[] a) {
        buff.append('[');
        final int len = a.length;
        for (int i = 0; i < len; i++) {
            buff.append(a[i]);
            if (i != len - 1)
                buff.append(", ");
        }
        buff.append(']');
    }

    /**
     * 追加字节数组参数
     *
     * @param buff 字符缓冲区
     * @param a    字节数组
     */
    private static void byteArrayAppend(StringBuilder buff, byte[] a) {
        buff.append('[');
        final int len = a.length;
        for (int i = 0; i < len; i++) {
            buff.append(a[i]);
            if (i != len - 1)
                buff.append(", ");
        }
        buff.append(']');
    }

    /**
     * 追加字符数组参数
     *
     * @param buff 字符缓冲区
     * @param a    字符数组
     */
    private static void charArrayAppend(StringBuilder buff, char[] a) {
        buff.append('[');
        final int len = a.length;
        for (int i = 0; i < len; i++) {
            buff.append(a[i]);
            if (i != len - 1)
                buff.append(", ");
        }
        buff.append(']');
    }

    /**
     * 追加短整型数组参数
     *
     * @param buff 字符缓冲区
     * @param a    短整型数组
     */
    private static void shortArrayAppend(StringBuilder buff, short[] a) {
        buff.append('[');
        final int len = a.length;
        for (int i = 0; i < len; i++) {
            buff.append(a[i]);
            if (i != len - 1)
                buff.append(", ");
        }
        buff.append(']');
    }

    /**
     * 追加整型数组参数
     *
     * @param buff 字符缓冲区
     * @param a    整型数组
     */
    private static void intArrayAppend(StringBuilder buff, int[] a) {
        buff.append('[');
        final int len = a.length;
        for (int i = 0; i < len; i++) {
            buff.append(a[i]);
            if (i != len - 1)
                buff.append(", ");
        }
        buff.append(']');
    }

    /**
     * 追加长整型数组参数
     *
     * @param buff 字符缓冲区
     * @param a    长整型数组
     */
    private static void longArrayAppend(StringBuilder buff, long[] a) {
        buff.append('[');
        final int len = a.length;
        for (int i = 0; i < len; i++) {
            buff.append(a[i]);
            if (i != len - 1)
                buff.append(", ");
        }
        buff.append(']');
    }

    /**
     * 追加浮点数组参数
     *
     * @param buff 字符缓冲区
     * @param a    浮点数组
     */
    private static void floatArrayAppend(StringBuilder buff, float[] a) {
        buff.append('[');
        final int len = a.length;
        for (int i = 0; i < len; i++) {
            buff.append(a[i]);
            if (i != len - 1)
                buff.append(", ");
        }
        buff.append(']');
    }

    /**
     * 追加双精度数组参数
     *
     * @param buff 字符缓冲区
     * @param a    双精度数组
     */
    private static void doubleArrayAppend(StringBuilder buff, double[] a) {
        buff.append('[');
        final int len = a.length;
        for (int i = 0; i < len; i++) {
            buff.append(a[i]);
            if (i != len - 1)
                buff.append(", ");
        }
        buff.append(']');
    }

    /**
     * 从参数数组中识别尾部的异常对象
     *
     * @param argArray 参数数组
     * @return 尾部的异常对象，未识别到时返回 null
     */
    public static Throwable getThrowableCandidate(final Object[] argArray) {
        return NormalizedParameters.getThrowableCandidate(argArray);
    }

    /**
     * 返回去除尾部异常对象后的参数数组副本
     *
     * @param argArray 原始参数数组
     * @return 去除最后一个元素后的新数组
     */
    public static Object[] trimmedCopy(final Object[] argArray) {
        return NormalizedParameters.trimmedCopy(argArray);
    }
}
