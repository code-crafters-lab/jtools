package org.codecrafterslab.agent;

import lombok.extern.slf4j.Slf4j;

import java.lang.instrument.Instrumentation;

/**
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time 2017/09/27 11:13
 */
@Slf4j
public class Launcher {

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
        Initializer.processAgent(log, agentArgs, inst, false);
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
        Initializer.processAgent(log, agentArgs, inst, true);
    }

}
