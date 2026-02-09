package org.codecrafterslab.agent;

import lombok.extern.slf4j.Slf4j;
import org.codecrafterslab.agent.core.AbstractAgent;
import org.codecrafterslab.agent.core.Environment;
import org.codecrafterslab.agent.api.ITransformer;

import java.util.*;

@Slf4j
public class Agent extends AbstractAgent<ITransformer> {

    private static final String CLASS_INCLUDE_PATTERN = System.getProperty("class.pattern.include", "");
    private static final String CLASS_EXCLUDE_PATTERN = System.getProperty("class.pattern.exclude", "");

    private final Environment environment;

    public Agent(Environment environment) {
        super(CLASS_INCLUDE_PATTERN.split(","), CLASS_EXCLUDE_PATTERN.split(","));
        this.environment = environment;
    }

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
                getClassNames().add(transformer.getName());
                List<ITransformer> transformers = getTransformerMap().computeIfAbsent(className, k -> new ArrayList<>());
                transformers.add(transformer);
            });
        }

    }

}
