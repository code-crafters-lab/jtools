package org.codecrafterslab.agent;

import org.codecrafterslab.agent.api.AppContext;
import org.codecrafterslab.agent.core.DefaultAppContext;
import org.codecrafterslab.agent.core.Environment;
import org.codecrafterslab.agent.core.plugin.PluginManager;
import org.codecrafterslab.agent.utils.AgentUtil;
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
     * 结尾的 JAR 文件（不区分大小写），用于识别需要追加到 Bootstrap ClassLoader 的依赖
     */
    private static final Pattern BOOTSTRAP_PATTERN = Pattern.compile(".*([.-]bootstrap\\.jar|bootstrap\\.jar)$", Pattern.CASE_INSENSITIVE);

    /**
     * 处理 Agent 入口逻辑
     *
     * @param log       日志
     * @param agentArgs Agent 参数
     * @param inst      Instrumentation 实例
     * @param attach    是否为 attach 模式
     */
    public static void processAgent(Logger log, String agentArgs, Instrumentation inst, boolean attach) {
        if (loaded) return;
        try {
            AgentUtil.getAgentJarFile().ifPresent(file -> {
                File root = file.getParentFile();
                loaded = true;

                File[] bootstrapJars = root.listFiles((dir, name) ->
                        name.endsWith(".jar") && BOOTSTRAP_PATTERN.matcher(name).matches()
                );
                File[] bootstraps = Optional.ofNullable(bootstrapJars).orElse(new File[0]);
                Arrays.stream(bootstraps)
                        .sorted(Comparator.comparingInt(Initializer::readBootstrapPriority))
                        .map(Initializer::openJarSafely)
                        .filter(Objects::nonNull)
                        .forEach(inst::appendToBootstrapClassLoaderSearch);

                URLClassLoader pluginClassLoader = buildPluginClassLoader(new File(root, "plugins"));

                Environment environment = new Environment(inst, file, agentArgs, attach, pluginClassLoader);
                Initializer.init(log, environment);
            });
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Can not locate `JAgent` jar file.", e);
            }
        }
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
     * 构建插件类加载器
     *
     * <p>从插件目录中读取所有非 bootstrap 的 JAR 文件，构建 {@link URLClassLoader}
     * 用于加载 Agent 插件。类加载器的父加载器为 {@code Initializer} 的加载器
     *
     * @param pluginsDir 插件目录
     * @return 插件类加载器
     */
    private static URLClassLoader buildPluginClassLoader(File pluginsDir) {
        File[] files = pluginsDir.listFiles((dir, name) ->
                name.endsWith(".jar") && !BOOTSTRAP_PATTERN.matcher(name).matches()
        );
        File[] jars = Optional.ofNullable(files).orElse(new File[0]);
        URL[] urls = Arrays.stream(jars)
                .map(f -> {
                    try {
                        return f.toURI().toURL();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toArray(URL[]::new);
        return new URLClassLoader(urls, Initializer.class.getClassLoader());
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
        AppContext appContext = new DefaultAppContext(
                environment.getAgentFile(),
                environment.getAppName(),
                environment.getVersion(),
                environment.getPluginClassLoader()
        );
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
