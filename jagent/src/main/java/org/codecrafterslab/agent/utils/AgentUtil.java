package org.codecrafterslab.agent.utils;

import lombok.extern.slf4j.Slf4j;
import org.codecrafterslab.agent.Dispatcher;
import org.codecrafterslab.agent.core.Environment;
import org.codecrafterslab.agent.core.plugin.PluginManager;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time 2020/04/29 21:54
 */
@Slf4j
public class AgentUtil {

    /**
     * 获取 JAR JAR 包所在路径
     */
    public static Optional<File> getAgentJarFile() {
        try {
            String path = AgentUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            File file = new File(path);
            return Optional.of(file);
        } catch (Exception e) {
            log.error("Get Agent Jar Path Error: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public static void init(Environment environment) {
        Dispatcher dispatcherAgent = new Dispatcher(environment);
        new PluginManager(dispatcherAgent, environment).loadPlugins();
        Instrumentation inst = environment.getInstrumentation();

        /* Agent_OnAttach */
        if (environment.isAttachMode()) {
            Set<Class<?>> classSet = new HashSet<>();

            /* 1. 注册类文件转换器 */
            inst.addTransformer(dispatcherAgent, inst.isRetransformClassesSupported());

            /* 2. 获取需要重新转换的类 */
            if (inst.isRetransformClassesSupported()) {
                Set<String> classNames = dispatcherAgent.getHookClassNames();
                Set<Pattern> includeClassNamePatterns = dispatcherAgent.getIncludeClassNamePattern();
                Set<Pattern> excludeClassNamePatterns = dispatcherAgent.getExcludeClassNamePattern();

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
            if (inst.isRetransformClassesSupported() && !classSet.isEmpty()) {
                Class<?>[] classes = classSet.toArray(new Class<?>[0]);
                if (log.isDebugEnabled()) {
                    List<String> names = classSet.stream().map(Class::getCanonicalName).collect(Collectors.toList());
                    log.debug("agent loaded and will transformer class : {}", names);
                }
                try {
                    /* 其中任何一个类不能转换将会抛出 UnmodifiableClassException 异常 */
                    inst.retransformClasses(classes);
                } catch (UnmodifiableClassException e) {
                    log.error(e.getMessage());
                }
            }
        }
        /* Agent_OnLoad */
        else {
            /* 1. 注册类文件转换器 */
            inst.addTransformer(dispatcherAgent);
        }

        /* 设置代理所需的本机方法前缀 */
        if (inst.isNativeMethodPrefixSupported()) {
            inst.setNativeMethodPrefix(dispatcherAgent, environment.getNativePrefix());
        }

    }
}
