package org.codecrafterslab.agent.api;

import java.lang.instrument.IllegalClassFormatException;

/**
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time 2019/08/02 15:16
 */
public interface ITransformer extends Comparable<ITransformer> {

    /**
     * 获取 {@code java} 语言规范定义的格式输出，如：{@code org.codecrafterslab.agent.core.Transformer}
     *
     * @return String
     */
    String getName();

    /**
     * 所需类的完整规范名称, 如: {@code org/codecrafterslab/agent/core/Transformer}.
     *
     * @return String
     */
    default String getHookClassName() {
        if (getName() == null || getName().isEmpty()) return null;
        return getName().replace('.', '/');
    }

    /**
     * 是否允许动态附加到 {@code JVM}
     *
     * @since 1.0.0
     */
    default boolean attachMode() {
        return true;
    }

    /**
     * 是否以{@code -javaagent}模式加载
     *
     * @since 1.0.0
     */
    default boolean javaagentMode() {
        return true;
    }

    /**
     * byte code
     *
     * @param classBytes byte[]
     * @return byte[]
     */
    default byte[] getCode(byte[] classBytes, int order) throws IllegalClassFormatException {
        return null;
    }

    /**
     * 转换器排序
     *
     * @return int
     */
    int getOrder();

    @Override
    default int compareTo(ITransformer o) {
        // 若 o 为 null 返回-1：表示当前对象排 < null（null排最后）
        if (o == null) return -1;
        return Integer.compare(getOrder(), o.getOrder());
    }

}
