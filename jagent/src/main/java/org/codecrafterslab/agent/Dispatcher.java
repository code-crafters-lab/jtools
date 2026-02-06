package org.codecrafterslab.agent;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.codecrafterslab.agent.api.AgentDispatcher;
import org.codecrafterslab.agent.core.Environment;
import org.codecrafterslab.agent.core.Transformer;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
@Slf4j
public class Dispatcher implements AgentDispatcher<Transformer> {
    private static final String DEFAULT_CLASS_OUT_DIR = System.getProperty("user.home") + File.separator + "code";
    private static final String CLASS_OUT_DIR = System.getProperty("class.out.dir", "");
    private static final String CLASS_INCLUDE_PATTERN = System.getProperty("class.pattern.include", "");
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

            // 输出 class 文件
            String dir = "".equals(CLASS_OUT_DIR) ? DEFAULT_CLASS_OUT_DIR : CLASS_OUT_DIR;
            this.exportClazzToFile(dir, className, CLASS_SUFFIX, result);

            return result;
        }
        return null;
    }

    public void exportClazzToFile(String dir, String fileName, String suffix, byte[] data) {
        if (data == null || data.length == 0) return;
        Path path = Paths.get(dir, fileName + suffix);
        if (log.isDebugEnabled()) {
            log.debug("class output to: {}", path);
        }
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to write class {} file to disk", fileName, e);
            }
        }
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
            Optional.ofNullable(transformer.getHookClassName()).ifPresent(className -> {
                hookClassNames.add(transformer.getCanonicalName());
                List<Transformer> transformers = transformersMap.computeIfAbsent(className, k -> new ArrayList<>());
                transformers.add(transformer);
            });
        }

    }
}
