package org.codecrafterslab.agent;

import org.codecrafterslab.agent.logger.Logger;
import org.codecrafterslab.agent.logger.impl.LoggerFactory;

import java.lang.instrument.Instrumentation;

/**
 * Agent 启动入口，提供 premain 和 agentmain 两种接入方式
 *
 * <p>premain 模式通过 -javaagent 参数在 JVM 启动时加载；
 * agentmain 模式通过 Attach API 动态附加到目标 JVM
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 */
public class Launcher {

    /** 日志记录器 */
    private final static Logger log = LoggerFactory.getLogger(Launcher.class);

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
                log.debug("agent for premain, agentArgs is [{}].", agentArgs);
            }
        }
        String app = parseAppName(agentArgs);
        Initializer.processAgent(log, app, inst, false);
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
                log.debug("agent for attach API, agentArgs is [{}].", agentArgs);
            }
        }
        String app = parseAppName(agentArgs);
        Initializer.processAgent(log, app, inst, true);
    }

    /**
     * 解析应用名称
     *
     * <p>从 agentArgs 中提取应用名称，当前直接返回原始参数。
     * 后续可扩展支持 {@code appName:param} 格式的解析
     *
     * @param agentArgs Agent 参数
     * @return 应用名称
     */
    private static String parseAppName(String agentArgs) {
        return agentArgs;
    }

}
