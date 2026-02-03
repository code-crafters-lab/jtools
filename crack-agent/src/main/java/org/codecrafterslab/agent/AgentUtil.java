package org.codecrafterslab.agent;


import com.sun.tools.attach.VirtualMachine;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.management.ManagementFactory;

@Slf4j
public class AgentUtil {
    private static String processId;

    public static final String AGENT_PATH = "ccl.agent.path";
    public static final String AGENT_FILE = "ccl.agent.file";

    /**
     * 获取当前程序进程ID
     *
     * @return String
     */
    public synchronized static String getProcessID() {
        if (null == processId) processId = ManagementFactory.getRuntimeMXBean().getName().split("@", 2)[0];
        return processId;
    }

    /**
     * 设置系统环境变量
     *
     * <p>ccl.agent.path</p>
     * <p>ccl.agent.file</p>
     * <p>
     * 用于在 Attach API 的时候提供 JAR 包所在路径
     */
    public static void setAgentJarInfo() {
        try {
            String path = AgentUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            File file = new File(path);
            System.setProperty(AGENT_FILE, file.getAbsolutePath());
            System.setProperty(AGENT_PATH, file.getParent());
        } catch (Exception e) {
            log.error("Get Agent Jar Path Error: {}", e.getMessage());
        }

    }


    private static void attach() {
        try {
            // 1. 获取目标 JVM 的进程 ID (PID)
            String targetPid = getProcessID();

            // 2. 附加到目标 JVM
            VirtualMachine vm = VirtualMachine.attach(targetPid);

            // 3. 获取指定要附加的 Agent 路径
            String agentPath = System.getProperty(AGENT_FILE);

            // 4. 加载 Agent
            // 4.1 方式一： 加载 java agent
            vm.loadAgent(agentPath,null);

            // 4.2 方式二： 加载 native agent
            // vm.loadAgentPath(agentPath, null);
            // vm.loadAgentLibrary("agent", null);

            // 5. 断开连接
            vm.detach();
            log.info("Agent 已成功附加到目标 JVM");
        } catch (Exception e) {
            log.error("附加 Agent 失败: {}", e.getMessage());
        }
    }

}
