package org.codecrafterslab.agent;

import lombok.extern.slf4j.Slf4j;
import org.codecrafterslab.agent.core.AbstractAgent;
import org.codecrafterslab.agent.core.Environment;
import org.codecrafterslab.agent.api.ITransformer;

import java.util.*;

/**
 * Agent 主实现类，负责任载入和分发字节码转换器
 *
 * <p>根据运行模式（attach / javaagent）自动过滤不支持的转换器，
 * 并将转换器注册到对应的目标类名下
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 */
@Slf4j
public class Agent extends AbstractAgent<ITransformer> {

    /**
     * 需要被拦截的类名正则匹配模式（通过系统属性 class.pattern.include 配置）
     */
    private static final String CLASS_INCLUDE_PATTERN = System.getProperty("class.pattern.include", "");

    /**
     * 需要排除的类名正则匹配模式（通过系统属性 class.pattern.exclude 配置）
     */
    private static final String CLASS_EXCLUDE_PATTERN = System.getProperty("class.pattern.exclude", "");

    /**
     * 运行时环境信息
     */
    private final Environment environment;

    /**
     * 创建 Agent 实例
     *
     * @param environment 运行环境
     */
    public Agent(Environment environment) {
        super(CLASS_INCLUDE_PATTERN.split(","), CLASS_EXCLUDE_PATTERN.split(","));
        this.environment = environment;
    }

    /**
     * 添加字节码转换器，会根据加载模式自动过滤
     *
     * <p>attach 模式下忽略 attachMode=false 的转换器，
     * javaagent 模式下忽略 javaagentMode=false 的转换器
     *
     * @param transformer 字节码转换器
     */
    public void addTransformer(ITransformer transformer) {
        if (null == transformer) return;

        if (environment.isAttachMode() && !transformer.attachMode()) {
            if (log.isDebugEnabled()) {
                log.debug("Transformer: {} is set to not load in attach mode, ignored.", transformer.getClass().getName());
            }
            return;
        }

        if (environment.isJavaagentMode() && !transformer.javaagentMode()) {
            log.debug("Transformer: {} is set to not load in javaagent mode, ignored.", transformer.getClass().getName());
            return;
        }

        synchronized (this) {
            Optional.ofNullable(transformer.getHookClassName()).ifPresent(className -> {
                if (className.isEmpty()) return;
                getClassNames().add(transformer.getClassName());
                List<ITransformer> transformers = getTransformerMap().computeIfAbsent(className, k -> new ArrayList<>());
                transformers.add(transformer);
            });
        }

    }

}
