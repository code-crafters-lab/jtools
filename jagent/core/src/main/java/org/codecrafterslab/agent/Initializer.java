package org.codecrafterslab.agent;

import org.codecrafterslab.agent.api.AppContext;
import org.codecrafterslab.agent.core.DefaultAppContext;
import org.codecrafterslab.agent.core.Environment;
import org.codecrafterslab.agent.core.plugin.PluginManager;
import org.codecrafterslab.agent.utils.AgentUtils;
import org.slf4j.Logger;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Agent 引导初始化器
 *
 * <p>负责定位 Agent JAR 文件、加载 bootstrap 依赖、
 * 构建 Environment 并初始化插件和转换器
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 */
public class Initializer {

    /**
     * 是否已加载，防止重复初始化
     */
    private static boolean loaded = false;

    /**
     * bootstrap JAR 文件匹配模式
     *
     * <p>匹配以 {@code .bootstrap.jar}、{@code -bootstrap.jar} 或 {@code bootstrap.jar}
     * 结尾的 JAR 文件（不区分大小写），用于扫描时过滤 bootstrap 类 JAR
     */
    private static final Pattern BOOTSTRAP_PATTERN = Pattern.compile(".*([.-]bootstrap\\.jar|bootstrap\\.jar)$", Pattern.CASE_INSENSITIVE);

    /**
     * 处理 Agent 入口逻辑
     *
     * <p>创建运行环境、构建 libs 目录的 Agent ClassLoader、
     * 加载 bootstrap 依赖，并初始化插件和转换器
     *
     * @param log       日志
     * @param agentArgs Agent 参数
     * @param inst      Instrumentation 实例
     * @param attach    是否为 attach 模式
     */
    public static void processAgent(Logger log, String agentArgs, Instrumentation inst, boolean attach) {
        if (loaded) return;
        try {
            AgentUtils.getAgentJarFile().ifPresent(file -> {
                loaded = true;

                Environment environment = new Environment(inst, file, agentArgs, attach);
                loadBootstrapJars(environment.getBootstrapDir(), inst);
                URLClassLoader agentClassLoader = buildAgentClassLoader(environment.getLibsDir());
                environment.setAgentClassLoader(agentClassLoader);

                Initializer.init(log, environment);
            });
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Can not locate `JAgent` jar file.", e);
            }
        }
    }

    /**
     * 加载 bootstrap 目录中的 JAR 到 Bootstrap ClassLoader
     *
     * <p>扫描 bootstrap 目录下的所有 JAR 文件，按优先级排序后依次追加到
     * Bootstrap ClassLoader 搜索路径中，使其对所有类可见
     *
     * @param bootstrapDir bootstrap 目录
     * @param inst         Instrumentation 实例
     */
    private static void loadBootstrapJars(File bootstrapDir, Instrumentation inst) {
        if (!bootstrapDir.exists() || !bootstrapDir.isDirectory()) return;
        File[] jars = bootstrapDir.listFiles((dir, name) -> name.endsWith(".jar"));
        File[] bootstraps = Optional.ofNullable(jars).orElse(new File[0]);
        Arrays.stream(bootstraps)
                .sorted(Comparator.comparingInt(Initializer::readBootstrapPriority))
                .map(Initializer::openJarSafely)
                .filter(Objects::nonNull)
                .forEach(inst::appendToBootstrapClassLoaderSearch);
    }

    /**
     * 扫描目录中的 JAR 文件并转换为 URL 数组
     *
     * @param dir 待扫描的目录
     * @return URL 数组
     */
    public static URL[] scanJarUrls(File dir) {
        File[] files = dir.listFiles((d, name) -> name.endsWith(".jar") && !BOOTSTRAP_PATTERN.matcher(name).matches());
        File[] jars = Optional.ofNullable(files).orElse(new File[0]);
        return Arrays.stream(jars)
                .map(f -> {
                    try {
                        return f.toURI().toURL();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toArray(URL[]::new);
    }

    /**
     * 读取 bootstrap JAR 的加载优先级
     *
     * <p>从 JAR 的 Manifest 中获取 {@code Plugin-Bootstrap-Priority} 属性值，
     * 优先级值越小越先加载；未设置或解析异常时返回 {@link Integer#MAX_VALUE} 作为默认最低优先级
     *
     * @param jar bootstrap JAR 文件
     * @return 优先级值，越小越优先
     */
    static int readBootstrapPriority(File jar) {
        try (JarFile jf = new JarFile(jar)) {
            String p = jf.getManifest().getMainAttributes()
                    .getValue("Plugin-Bootstrap-Priority");
            return p == null ? Integer.MAX_VALUE : Integer.parseInt(p);
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    /**
     * 安全打开 JAR 文件
     *
     * <p>打开指定文件并返回 {@link JarFile} 实例，当文件不存在或无法打开时返回 {@code null}
     *
     * @param f JAR 文件
     * @return JarFile 实例，打开失败时返回 null
     */
    private static JarFile openJarSafely(File f) {
        try {
            return new JarFile(f);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 构建 Agent 类加载器
     *
     * <p>从 libs 目录中加载所有 JAR 文件，构建 {@link URLClassLoader}
     * 用于加载 Agent 核心库。类加载器的父加载器为 {@code Initializer} 的加载器
     *
     * @param libsDir 库目录
     * @return Agent 类加载器
     */
    private static URLClassLoader buildAgentClassLoader(File libsDir) {
        URL[] libUrls = scanJarUrls(libsDir);
        return new URLClassLoader(libUrls, Initializer.class.getClassLoader());
    }

    /**
     * 初始化 Agent 核心逻辑
     *
     * <p>包括注册转换器、attach 模式下重转换已加载类、
     * 设置本机方法前缀等
     *
     * @param log         日志
     * @param environment 运行环境
     */
    private static void init(Logger log, Environment environment) {
        Agent agent = new Agent(environment);
        AppContext appContext = new DefaultAppContext(environment);
        PluginManager.loadPlugins(agent, appContext);
        Instrumentation inst = environment.getInstrumentation();

        /* 1. 注册类文件转换器 */
        boolean retransformClassesSupported = inst.isRetransformClassesSupported();
        inst.addTransformer(agent, retransformClassesSupported);

        if (environment.isAttachMode()) {
            /* 2. 获取需要重新转换的类 */
            Set<Class<?>> classSet = new HashSet<>();
            if (retransformClassesSupported) {
                Set<String> classNames = agent.getClassNames();
                Set<Pattern> includeClassNamePatterns = agent.getIncludeClassNamePattern();
                Set<Pattern> excludeClassNamePatterns = agent.getExcludeClassNamePattern();

                classSet = Arrays.stream((Class<?>[]) inst.getAllLoadedClasses())
                        .filter(clazz -> !clazz.isArray())
                        .filter(clazz -> clazz.getCanonicalName() != null && !clazz.getCanonicalName().isEmpty())
                        .filter(clazz -> excludeClassNamePatterns.stream().noneMatch(
                                pattern -> pattern.matcher(clazz.getCanonicalName().replace(".", "/")).matches()
                        ))
                        .filter(clazz -> {
                            boolean b1 = includeClassNamePatterns.stream()
                                    .anyMatch(pattern -> pattern.matcher(clazz.getCanonicalName().replace(".", "/")).matches());
                            boolean b2 = classNames.stream().anyMatch(name -> name.equals(clazz.getCanonicalName()));
                            return b1 || b2;
                        })
                        .collect(Collectors.toSet());
            }

            /* 3. 重新转换类 */
            if (retransformClassesSupported && !classSet.isEmpty()) {
                Class<?>[] classes = classSet.toArray(new Class<?>[0]);
                if (log.isInfoEnabled()) {
                    List<String> names = classSet.stream().map(Class::getCanonicalName).collect(Collectors.toList());
                    log.info("agent loaded and will transformer class : {}", names);
                }
                try {
                    /* 其中任何一个类不能转换将会抛出 UnmodifiableClassException 异常 */
                    inst.retransformClasses(classes);
                } catch (UnmodifiableClassException e) {
                    log.error(e.getMessage());
                }
            }
        }

        /* 4. 设置代理所需的本机方法前缀 */
        if (inst.isNativeMethodPrefixSupported()) {
            inst.setNativeMethodPrefix(agent, environment.getNativePrefix());
        }

    }

}
