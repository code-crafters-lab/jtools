package org.codecrafterslab.agent.api;

import java.lang.instrument.IllegalClassFormatException;

/**
 * 字节码转换器接口，用于定义类的字节码修改逻辑
 *
 * <p>实现此接口可自定义字节码转换行为，控制加载模式和排序
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time 2019/08/02 15:16
 */
public interface ITransformer extends Comparable<ITransformer> {

    /**
     * 获取目标类的全限定名（Java 语言规范定义的格式，如 org.codecrafterslab.agent.core.Transformer）
     *
     * @return 目标类的全限定名
     */
    String getClassName();

    /**
     * 获取目标类的内部名称，如 org/codecrafterslab/agent/core/Transformer
     *
     * @return 目标类的内部名称，name 为空时返回 null
     */
    default String getHookClassName() {
        if (getClassName() == null || getClassName().isEmpty()) return null;
        return getClassName().replace('.', '/');
    }

    /**
     * 是否允许 attach 模式动态加载
     *
     * @return true 表示允许 attach 模式
     * @since 1.0.0
     */
    default boolean attachMode() {
        return true;
    }

    /**
     * 是否允许 -javaagent 模式加载
     *
     * @return true 表示允许 javaagent 模式
     * @since 1.0.0
     */
    default boolean javaagentMode() {
        return true;
    }

    /**
     * 执行字节码转换
     *
     * @param classBytes 原始字节码
     * @param order      转换器执行顺序
     * @return 转换后的字节码，null 表示不进行转换
     * @throws IllegalClassFormatException 字节码格式错误
     */
    default byte[] getCode(byte[] classBytes, int order) throws IllegalClassFormatException {
        return null;
    }

    /**
     * 获取转换器优先级，值越小越先执行
     *
     * @return 排序值
     */
    int getOrder();

    @Override
    default int compareTo(ITransformer o) {
        // 若 o 为 null 返回-1：表示当前对象排 < null（null排最后）
        if (o == null) return -1;
        return Integer.compare(getOrder(), o.getOrder());
    }

}
