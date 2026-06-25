package org.codecrafterslab.agent.utils;

import java.util.Random;

/**
 * 字符串工具类，提供基础的字符串判空、随机方法名生成和类型转换功能
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 */
public class StringUtils {
    private static final String METHOD_NAME_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz$_0123456789";

    /**
     * 判断字符串是否为空
     *
     * @param str 待判断字符串
     * @return true 表示字符串为 null 或空字符串
     */
    public static boolean isEmpty(String str) {
        return null == str || str.isEmpty();
    }

    /**
     * 生成指定长度的随机方法名
     *
     * <p>首字符从字母、$ 和 _ 中选取，后续字符额外包含数字，
     * 确保生成的方法名符合 Java 标识符规范
     *
     * @param length 方法名长度
     * @return 随机生成的方法名
     */
    public static String randomMethodName(int length) {
        int i = 0;
        if (i == length) {
            return "";
        }

        char[] buffer = new char[length];
        Random rnd = new Random();

        buffer[i++] = METHOD_NAME_CHARS.charAt(rnd.nextInt(54));
        while (i < length) {
            buffer[i++] = METHOD_NAME_CHARS.charAt(rnd.nextInt(64));
        }

        return new String(buffer);
    }

    /**
     * 将字符串转换为 Long 类型
     *
     * @param val 待转换字符串
     * @return Long 值，转换失败或入参为 null 时返回 null
     */
    public static Long toLong(String val) {
        if (null == val) {
            return null;
        }

        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
