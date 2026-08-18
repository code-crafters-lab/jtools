package org.codecrafterslab.agent.logger;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 日志输出目标枚举
 *
 * <p>定义日志可以输出的目标通道，支持通过逗号分隔的系统属性字符串
 * 解析出多个输出目标（如 {@code console,file}）
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time 2020/04/29 14:13
 */
public enum Output {

    /** 控制台输出 */
    CONSOLE,
    /** 文件输出 */
    FILE;

    /**
     * 解析输出目标字符串为输出目标列表
     *
     * <p>按逗号分割原始字符串，逐项匹配枚举名称（忽略大小写），
     * 无法识别的项将被忽略，返回的列表仅包含有效命中项
     *
     * @param out 逗号分隔的输出目标字符串
     * @return 解析出的输出目标列表，可为空列表
     */
    public static List<Output> from(String out) {
        return Arrays.stream(out.trim().split(","))
                .map(s -> Arrays.stream(values())
                        .filter(v -> v.name().equalsIgnoreCase(s))
                        .findFirst().orElse(null)
                ).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
