package org.codecrafterslab.agent;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.codecrafterslab.agent.core.Environment;
import org.codecrafterslab.agent.core.Transformer;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
@Slf4j
public class Dispatcher implements ClassFileTransformer {
    private static final String DEFAULT_CLASS_OUT_DIR = System.getProperty("user.home") + File.separator + "code";
    private static final String CLASS_OUT_DIR = System.getProperty("class.out.dir", "");
    private static final String CLASS_INCLUDE_PATTERN = System.getProperty("class.pattern", "");
    private static final String CLASS_EXCLUDE_PATTERN = System.getProperty("class.pattern.exclude", "");
    private static final String CLASS_SUFFIX = ".class";

    private final Map<String, List<Transformer>> transformersMap = new HashMap<>();
    private final Environment environment;
    /**
     * 需要被拦截类
     */
    private final Set<String> hookClassNames = new TreeSet<>();
    /**
     * 需要排除类正则匹配
     */
    private final Set<Pattern> excludeClassNamePattern;
    /**
     * 需要被拦截类正则匹配
     */
    private final Set<Pattern> includeClassNamePattern;

    public Dispatcher(Environment environment) {
        this.environment = environment;
        this.includeClassNamePattern = Arrays.stream(CLASS_INCLUDE_PATTERN.split(","))
                .filter(s -> !s.isEmpty())
                .map(Pattern::compile)
                .collect(Collectors.toSet());
        this.excludeClassNamePattern = Arrays.stream(CLASS_EXCLUDE_PATTERN.split(","))
                .filter(s -> !s.isEmpty())
                .map(Pattern::compile)
                .collect(Collectors.toSet());
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (transformersMap.containsKey(className)) {
            if (log.isDebugEnabled()) {
                log.debug("Intercepts the entry class ：{}", className);
            }
            List<Transformer> transformers = transformersMap.get(className);
            int order = 0;
            byte[] result = classfileBuffer;
            for (Transformer transformer : transformers) {
                result = transformer.transform(loader, className, classBeingRedefined, protectionDomain, result, order++);
            }
            return classfileBuffer;
        }
        return null;
    }

    public void addTransformer(Transformer transformer) {
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
            String className = transformer.getHookClassName();
            if (null == className) {
//                globalTransformers.add(transformer);
//                if (transformer.isManager()) {
//                    manageTransformers.add(transformer);
//                }
                return;
            }

            hookClassNames.add(transformer.getCanonicalName());
            List<Transformer> transformers = transformersMap.computeIfAbsent(className, k -> new ArrayList<>());
            transformers.add(transformer);
        }

    }

    public void addTransformers(Collection<Transformer> transformers) {
        if (null == transformers) return;
        for (Transformer transformer : transformers) {
            addTransformer(transformer);
        }
    }
}
