package org.codecrafterslab.agent.core;

import lombok.extern.slf4j.Slf4j;
import org.codecrafterslab.agent.api.IAgent;
import org.codecrafterslab.agent.api.ITransformer;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractAgent<T extends ITransformer> implements IAgent<T> {
    private static final String DEFAULT_CLASS_OUT_DIR = System.getProperty("user.home") + File.separator + "code";
    private static final String CLASS_OUT_DIR = System.getProperty("class.out.dir", "");
    private static final String CLASS_SUFFIX = ".class";
    /**
     * 需要被拦截类正则匹配
     */
    private final Set<Pattern> includeClassNamePattern = new HashSet<>();
    /**
     * 需要排除类正则匹配
     */
    private final Set<Pattern> excludeClassNamePattern = new HashSet<>();
    /**
     * 需要被拦截处理的类
     */
    private final Set<String> classNames = new TreeSet<>();

    private final Map<String, List<ITransformer>> transformerMap = new HashMap<>();

    public AbstractAgent() {
        this(null, null);
    }

    public AbstractAgent(String[] include, String[] exclude) {
        Optional.ofNullable(include).ifPresent(i -> this.includeClassNamePattern.addAll(
                Arrays.stream(i).filter(s -> s != null && !s.isEmpty())
                        .map(Pattern::compile)
                        .collect(Collectors.toSet())));
        Optional.ofNullable(exclude).ifPresent(e -> this.excludeClassNamePattern.addAll(
                Arrays.stream(e).filter(s -> s != null && !s.isEmpty())
                        .map(Pattern::compile)
                        .collect(Collectors.toSet())));
    }

    @Override
    public Set<Pattern> getIncludeClassNamePattern() {
        return includeClassNamePattern;
    }

    @Override
    public Set<Pattern> getExcludeClassNamePattern() {
        return excludeClassNamePattern;
    }

    @Override
    public Set<String> getClassNames() {
        return classNames;
    }

    @Override
    public Map<String, List<ITransformer>> getTransformerMap() {
        return transformerMap;
    }

    @Override
    public void exportClazzToFile(String outDir, String className, String suffix, byte[] data) {
        if (data == null || data.length == 0) return;
        Path path = Paths.get(outDir, String.format("%s%s", className, suffix));
        if (log.isDebugEnabled()) {
            log.debug("class output to: {}", path);
        }
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to write class {} file to disk", className, e);
            }
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classBuffer) throws IllegalClassFormatException {
        if (transformerMap.containsKey(className)) {
            if (log.isDebugEnabled()) {
                log.debug("Intercepts the entry class ：{}", className);
            }
            // TODO: 转换器不知道是哪个插件的，这里排序还好存在问题，(ITransformer 可获取插件对象，插件也支持排序)
            List<ITransformer> transformers = transformerMap.get(className).stream().sorted().collect(Collectors.toList());

            for (int i = 0; i < transformers.size(); i++) {
                ITransformer transformer = transformers.get(i);
                if (transformer instanceof ClassFileTransformer) {
                    classBuffer = ((ClassFileTransformer) transformer).transform(loader, className, classBeingRedefined, protectionDomain, classBuffer);
                } else {
                    classBuffer = transformer.getCode(classBuffer, i);
                }
            }

            // 输出 修改后的 class 文件
            this.exportClazzToFile(CLASS_OUT_DIR.isEmpty() ? DEFAULT_CLASS_OUT_DIR : CLASS_OUT_DIR, className, CLASS_SUFFIX, classBuffer);

            return classBuffer;
        }
        return null;
    }
}
