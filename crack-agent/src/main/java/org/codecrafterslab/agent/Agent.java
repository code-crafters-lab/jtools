package org.codecrafterslab.agent;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class Agent {

    private static void processAgent(String agentArgs, Instrumentation inst, boolean attach) {
        /* 解析参数 */
        String transformerClassName = System.getProperty("transformer.class.name",
                "org.codecrafterslab.data.impl.DefaultDataGuard");

        DataGuardClassFileTransformer classFileTransformer = new DataGuardClassFileTransformer();
        File file = new File("");
        try {
            inst.appendToBootstrapClassLoaderSearch(new JarFile(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        /* Agent_OnAttach */
        if (attach) {
            Set<Class<?>> classSet = null;

            /* 1. 注册类文件转换器 */
            inst.addTransformer(classFileTransformer, inst.isRetransformClassesSupported());

            /* 2. 获取需要重新转换的类 */
            if (inst.isRetransformClassesSupported()) {
                /* 2.1 所有已经加载的类 */
                Stream<Class<?>> allLoadedClasses = Arrays.stream((Class<?>[]) inst.getAllLoadedClasses())
                        .filter(clazz -> clazz.getCanonicalName() != null && !clazz.getCanonicalName().isEmpty());

                /* 2.2 添加需要转换的类 */
                classSet = allLoadedClasses
                        .filter(clazz -> transformerClassName.equals(clazz.getCanonicalName()))
                        .collect(Collectors.toSet());
            }

            /* 3. 重新转换类 */
            if (inst.isRetransformClassesSupported() && classSet != null && !classSet.isEmpty()) {
                Class<?>[] classes = classSet.toArray(new Class<?>[0]);
                if (log.isDebugEnabled()) {
                    List<String> names = classSet.stream().map(Class::getCanonicalName).collect(Collectors.toList());
                    log.debug("agent loaded and will retransformer class : {}", names);
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
            /* 1. 将当前 jar 包信息设置到系统环境变量 */
            AgentUtil.setAgentJarInfo();
            /* 2. 注册类文件转换器 */
            inst.addTransformer(classFileTransformer);
        }

        /* 设置代理所需的本机方法前缀 */
        if (inst.isNativeMethodPrefixSupported()) {
            inst.setNativeMethodPrefix(classFileTransformer, "");
        }

    }

    /**
     * JVM 首先尝试在代理类上调用以下方法
     *
     * @param agentArgs agentArgs
     * @param inst      inst
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        if (log.isDebugEnabled()) {
            if (null == agentArgs || agentArgs.isEmpty()) {
                log.debug("agent for premain");
            } else {
                log.debug("agent for premain,agentArgs is [{}].", agentArgs);
            }
        }
        processAgent(agentArgs, inst, false);
    }

    /**
     * Attach API
     *
     * @param agentArgs agentArgs
     * @param inst      inst
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        if (log.isDebugEnabled()) {
            if (null == agentArgs || agentArgs.isEmpty()) {
                log.debug("agent for attach API");
            } else {
                log.debug("agent for attach API,agentArgs is [{}].", agentArgs);
            }
        }
        processAgent(agentArgs, inst, true);
    }

}
