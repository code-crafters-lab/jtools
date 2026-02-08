package org.codecrafterslab.agent.utils;

import com.sun.tools.attach.VirtualMachine;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.Manifest;

/**
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time 2020/04/29 21:54
 */
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
        if (null == processId) {
            processId = ManagementFactory.getRuntimeMXBean().getName().split("@", 2)[0];
        }
        return processId;
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
            vm.loadAgent(agentPath, null);

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

    public static Manifest getManifest(Class<?> clazz) {
        String className = clazz.getName().replace('.', '/') + ".class";
        URL classUrl = clazz.getClassLoader().getResource(className);
        if (classUrl == null) {
            return null; // 类资源不存在，返回空
        }

        // 2. 仅处理 Jar 包中的类（非 Jar 包场景直接返回空）
        if (!"jar".equals(classUrl.getProtocol())) {
            return null;
        }

        JarURLConnection jarConn = null;
        try {
            // 3. 打开 Jar 连接并读取 MANIFEST.MF
            jarConn = (JarURLConnection) classUrl.openConnection();
            // MANIFEST 文件不存在，返回空
            return jarConn.getManifest();
        } catch (IOException e) {
            // 捕获 IO 异常（如 Jar 包损坏、MANIFEST 读取失败），返回空
            return null;
        } finally {
            // 5. 关闭连接，避免资源泄漏
            if (jarConn != null) {
                try {
                    jarConn.getInputStream().close();
                } catch (IOException e) {
                    // 关闭流异常不影响主逻辑，静默处理
                }
            }
        }
    }
}
