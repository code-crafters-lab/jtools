package org.codecrafterslab.agent.api;

import java.lang.instrument.ClassFileTransformer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Agent 核心接口，定义字节码转换器的管理与分发能力
 *
 * <p>继承 ClassFileTransformer，集成到 JVM 的类加载流程中，
 * 支持按类名匹配、排除和转换器注册
 *
 * @param <T> 转换器类型
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time 2017/09/27 11:15
 */
public interface IAgent<T extends ITransformer> extends ClassFileTransformer {

    /**
     * 获取需要被拦截的类名正则匹配模式
     *
     * @return 包含模式集合
     */
    Set<Pattern> getIncludeClassNamePattern();

    /**
     * 获取需要排除的类名正则匹配模式
     *
     * @return 排除模式集合
     */
    Set<Pattern> getExcludeClassNamePattern();

    /**
     * 获取所有需要被拦截的目标类名
     *
     * @return 目标类名集合
     */
    Set<String> getClassNames();

    /**
     * 获取类名到转换器列表的映射关系
     *
     * @return 转换器映射表
     */
    Map<String, List<ITransformer>> getTransformerMap();

    /**
     * 添加一个字节码转换器
     *
     * @param transformer 转换器实例
     */
    void addTransformer(T transformer);

    /**
     * 批量添加字节码转换器
     *
     * @param transformers 转换器集合
     */
    default void addTransformers(Collection<T> transformers) {
        if (null == transformers) return;
        for (T transformer : transformers) {
            addTransformer(transformer);
        }
    }

    /**
     * 导出 class 文件到磁盘
     *
     * @param outDir    输出目录
     * @param className 类名
     * @param suffix    文件后缀
     * @param data      字节码数据
     */
    default void exportClazzToFile(String outDir, String className, String suffix, byte[] data) {

    }
}
