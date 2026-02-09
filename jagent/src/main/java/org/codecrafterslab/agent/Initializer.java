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
import java.util.*;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Initializer {
    private static boolean loaded = false;

    public static void processAgent(Logger log, String agentArgs, Instrumentation inst, boolean attach) {
        if (loaded) return;
        try {
            AgentUtil.getAgentJarFile().ifPresent(file -> {
                loaded = true;
                File[] files = Optional.ofNullable(file.getParentFile()
                        .listFiles((dir, name) -> name.endsWith("jagent-bootstrap-0.1.0.jar"))).orElse(new File[0]);

                Stream.of(files).map(f -> {
                    try {
                        return new JarFile(f);
                    } catch (Exception e) {
                        return null;
                    }
                }).filter(Objects::nonNull).findFirst().ifPresent(inst::appendToBootstrapClassLoaderSearch);

                Environment environment = new Environment(inst, file, agentArgs, attach);
                Initializer.init(log, environment);
            });
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Can not locate `JAgent` jar file.", e);
            }
        }
    }

    private static void init(Logger log, Environment environment) {
        Agent agent = new Agent(environment);
        AppContext appContext = new DefaultAppContext(environment.getAgentFile());
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
