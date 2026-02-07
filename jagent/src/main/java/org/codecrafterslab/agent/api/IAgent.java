package org.codecrafterslab.agent.api;

import java.lang.instrument.ClassFileTransformer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time @time 2017/09/27 11:15
 */
public interface IAgent<T extends ITransformer> extends ClassFileTransformer {

    /**
     * 需要被拦截类正则匹配
     *
     * @return List<Pattern>
     */
    Set<Pattern> getIncludeClassNamePattern();

    /**
     * 需要排除类正则匹配
     *
     * @return {@code List<Pattern>}
     */
    Set<Pattern> getExcludeClassNamePattern();

    /**
     * 获取所有被拦截类
     *
     * @return {@code Set<String>}
     *
     */
    Set<String> getClassNames();

    /**
     * 获取需要被转换的类
     *
     * @return {@code Map<String, List<ITransformer>>}
     */
    Map<String, List<ITransformer>> getTransformerMap();

    /**
     * 添加一个{@link ITransformer}
     *
     * @param transformer 字节码转换器
     */
    void addTransformer(T transformer);

    /**
     * 添加多个{@link ITransformer}
     *
     * @param transformers 字节码转换器集合
     */
    default void addTransformers(Collection<T> transformers) {
        if (null == transformers) return;
        for (T transformer : transformers) {
            addTransformer(transformer);
        }
    }

    /**
     * 导出 class 文件
     *
     * @param outDir    输出目录
     * @param className 类名
     * @param suffix    后缀
     * @param data      字节码
     */
    default void exportClazzToFile(String outDir, String className, String suffix, byte[] data) {

    }
}
