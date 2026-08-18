package org.codecrafterslab.agent.core;

import org.codecrafterslab.agent.api.IAgent;
import org.codecrafterslab.agent.api.ITransformer;
import org.codecrafterslab.agent.logger.Logger;
import org.codecrafterslab.agent.logger.impl.LoggerFactory;

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

/**
 * Agent 抽象基类，实现类文件转换核心逻辑
 *
 * <p>负责管理转换器映射、类名匹配规则和字节码转换流程，
 * 是 IAgent 接口的标准实现
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @param <T> 转换器类型
 */
public abstract class AbstractAgent<T extends ITransformer> implements IAgent<T> {

    /** 日志记录器，按运行类自动命名 */
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * 默认 class 文件输出目录（用户主目录下的 code 文件夹）
     */
    private static final String DEFAULT_CLASS_OUT_DIR = System.getProperty("user.home") + File.separator + "code";

    /**
     * 自定义 class 文件输出目录（通过系统属性 ccl.agent.class.out.dir 配置）
     */
    private static final String CLASS_OUT_DIR = System.getProperty("ccl.agent.class.out.dir", "");

    /**
     * class 文件后缀
     */
    private static final String CLASS_SUFFIX = ".class";

    /**
     * 需要被拦截的类名正则匹配模式集合
     */
    private final Set<Pattern> includeClassNamePattern = new HashSet<>();

    /**
     * 需要排除的类名正则匹配模式集合
     */
    private final Set<Pattern> excludeClassNamePattern = new HashSet<>();

    /**
     * 需要被拦截处理的目标类名集合
     */
    private final Set<String> classNames = new TreeSet<>();

    /**
     * 类名到转换器列表的映射关系
     */
    private final Map<String, List<ITransformer>> transformerMap = new HashMap<>();

    /**
     * 使用默认参数创建 Agent
     */
    public AbstractAgent() {
        this(null, null);
    }

    /**
     * 创建 Agent，指定类名匹配规则
     *
     * @param include 需要拦截的类名正则表达式数组
     * @param exclude 需要排除的类名正则表达式数组
     */
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
        if (log.isTraceEnabled()) {
            log.trace("class output to: {}", path);
        }
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            if (log.isWarnEnabled()) {
                log.warn("Failed to write class {} file to disk", className, e);
            }
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classBuffer) throws IllegalClassFormatException {
        if (transformerMap.containsKey(className)) {
            if (log.isTraceEnabled()) {
                log.trace("Intercepts the entry class ：{}", className);
            }
            // TODO: 转换器不知道是哪个插件的，这里排序还存在问题，(ITransformer 可获取插件对象，插件也支持排序)
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
            if (this.isOutputInterceptedModifiedClasses()){
                this.exportClazzToFile(CLASS_OUT_DIR.isEmpty() ? DEFAULT_CLASS_OUT_DIR : CLASS_OUT_DIR, className, CLASS_SUFFIX, classBuffer);
            }

            return classBuffer;
        }
        return null;
    }
}
