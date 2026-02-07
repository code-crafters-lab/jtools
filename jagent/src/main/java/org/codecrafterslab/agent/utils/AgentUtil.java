package org.codecrafterslab.agent.utils;

import com.sun.tools.attach.VirtualMachine;
import lombok.extern.slf4j.Slf4j;
import org.codecrafterslab.agent.Agent;
import org.codecrafterslab.agent.api.AppContext;
import org.codecrafterslab.agent.core.DefaultAppContext;
import org.codecrafterslab.agent.core.Environment;
import org.codecrafterslab.agent.core.plugin.PluginManager;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.management.ManagementFactory;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    public static void init(Environment environment) {
        Agent agent = new Agent(environment);
        AppContext appContext = new DefaultAppContext(environment.getAgentFile());
        PluginManager.loadPlugins(agent, appContext);
//        new PluginManager(agent, environment).loadPlugins();

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
