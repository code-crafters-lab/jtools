package org.codecrafterslab.agent.core;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.codecrafterslab.agent.utils.AgentUtils;
import org.codecrafterslab.agent.utils.StringUtils;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.URLClassLoader;

/**
 * Agent 运行时环境，封装了所有运行时需要的基础信息
 *
 * <p>包括 Instrumentation 实例、目录结构、进程信息等。
 * 除 {@code agentClassLoader} 在构造后由 Initializer 设置外，
 * 其余字段均为不可变的环境快照
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 */
@Getter
@ToString
public final class Environment {

    /**
     * Instrumentation 实例，用于字节码操作
     */
    private final Instrumentation instrumentation;

    /**
     * Agent JAR 文件
     */
    private final File agentFile;

    /**
     * 是否为 attach 模式（非 -javaagent 启动）
     */
    private final boolean attachMode;

    /**
     * 应用基础目录
     */
    private final File baseDir;

    /**
     * 配置目录
     */
    private final File configDir;

    /**
     * Bootstrap 目录，存放需要追加到 Bootstrap ClassLoader 的 JAR
     */
    private final File bootstrapDir;

    /**
     * 核心库目录，存放 Agent 运行时依赖的 JAR
     */
    private final File libsDir;

    /**
     * 插件目录
     */
    private final File pluginsDir;

    /**
     * 日志目录
     */
    private final File logsDir;

    /**
     * 当前进程 ID
     */
    private final String pid;

    /**
     * Agent 版本号
     */
    private final String version;

    /**
     * Agent 版本号数字格式
     */
    private final int versionNumber;

    /**
     * 应用名称
     */
    private final String appName;

    /**
     * 本机方法前缀，用于 native 方法注入
     */
    private final String nativePrefix;

    /**
     * 已禁用插件的 JAR 后缀名
     */
    private final String disabledPluginSuffix;

    /**
     * Agent ClassLoader，用于加载 libs 目录中的 JAR
     */
    @Setter
    private URLClassLoader agentClassLoader;

    /**
     * 创建环境实例
     *
     * @param instrumentation Instrumentation 实例
     * @param agentFile       Agent JAR 文件
     * @param attachMode      是否为 attach 模式
     */
    public Environment(Instrumentation instrumentation, File agentFile, boolean attachMode) {
        this(instrumentation, agentFile, null, attachMode);
    }

    /**
     * 创建环境实例
     *
     * @param instrumentation Instrumentation 实例
     * @param agentFile       Agent JAR 文件
     * @param app             应用名称
     * @param attachMode      是否为 attach 模式
     */
    public Environment(Instrumentation instrumentation, File agentFile, String app, boolean attachMode) {
        this(instrumentation, agentFile, app, attachMode, null);
    }

    /**
     * 创建环境实例（含 Agent ClassLoader）
     *
     * @param instrumentation  Instrumentation 实例
     * @param agentFile        Agent JAR 文件
     * @param app              应用名称
     * @param attachMode       是否为 attach 模式
     * @param agentClassLoader Agent ClassLoader，可为 {@code null}
     */
    public Environment(Instrumentation instrumentation, File agentFile, String app, boolean attachMode, URLClassLoader agentClassLoader) {
        this.instrumentation = instrumentation;
        this.agentFile = agentFile;
        this.attachMode = attachMode;
        this.baseDir = agentFile.getParentFile();

        if (StringUtils.isEmpty(app)) {
            this.appName = "";
            this.bootstrapDir = new File(baseDir, "bootstrap");
            this.configDir = new File(baseDir, "conf");
            this.libsDir = new File(baseDir, "libs");
            this.pluginsDir = new File(baseDir, "plugins");
            this.logsDir = new File(baseDir, "logs");
        } else {
            this.appName = app;
            this.bootstrapDir = new File(baseDir, String.format("%s/bootstrap", appName));
            this.configDir = new File(baseDir, String.format("%s/conf", appName));
            this.libsDir = new File(baseDir, String.format("%s/libs", appName));
            this.pluginsDir = new File(baseDir, String.format("%s/plugins", appName));
            this.logsDir = new File(baseDir, String.format("%s/logs", appName));
        }

        this.pid = AgentUtils.getProcessID();
        this.version = "2026.1";
        this.versionNumber = 20260100;
        this.nativePrefix = "ccl_";
        this.disabledPluginSuffix = ".disabled.jar";
        this.agentClassLoader = agentClassLoader;

    }

    /**
     * 是否为 -javaagent 模式启动
     *
     * @return true 表示 javaagent 模式
     */
    public boolean isJavaagentMode() {
        return !attachMode;
    }
}
