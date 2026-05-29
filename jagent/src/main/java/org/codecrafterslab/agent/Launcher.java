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
     * -javaagent 模式入口方法
     *
     * <p>JVM 启动时通过 -javaagent 参数自动调用，
     * 在目标类加载前进行字节码转换
     *
     * @param agentArgs Agent 参数
     * @param inst      Instrumentation 实例
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
     * attach 模式入口方法
     *
     * <p>通过 VirtualMachine.attach() 动态附加到已运行 JVM，
     * 支持热插拔字节码转换
     *
     * @param agentArgs Agent 参数
     * @param inst      Instrumentation 实例
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
