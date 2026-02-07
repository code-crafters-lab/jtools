package org.codecrafterslab.agent;

import lombok.extern.slf4j.Slf4j;
import org.codecrafterslab.agent.core.Environment;
import org.codecrafterslab.agent.utils.AgentUtil;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

/**
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time 2017/09/27 11:13
 */
@Slf4j
public class Launcher {
    private static boolean loaded = false;

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

    private static void processAgent(String agentArgs, Instrumentation inst, boolean attach) {
        if (loaded) return;
        try {
            AgentUtil.getAgentJarFile().ifPresent(file -> {
                loaded = true;
                JarFile jarFile;
                try {
                    jarFile = new JarFile(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
//                inst.appendToSystemClassLoaderSearch(jarFile);
                Environment environment = new Environment(inst, file, agentArgs, attach);
                AgentUtil.init(environment);
            });
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Can not locate `JAgent` jar file.", e);
            }
        }
    }

}
