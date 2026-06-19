package org.codecrafterslab.agent.core;

import com.janetfilter.core.utils.StringUtils;
import org.codecrafterslab.agent.api.AppContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * AppContext 的默认实现，负责管理应用各功能目录
 *
 * <p>根据 Agent JAR 文件位置自动构建基础目录、
 * 插件目录、配置目录和日志目录
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 */
public class DefaultAppContext implements AppContext {

    /**
     * 应用名称
     */
    private final String appName;

    /**
     * 应用版本
     */
    private final String appVersion;

    /**
     * 应用基础目录（Agent JAR 所在目录）
     */
    private final File baseDir;

    /**
     * 插件目录
     */
    private final File pluginDir;

    /**
     * 配置目录
     */
    private final File configDir;

    /**
     * 日志目录
     */
    private final File logsDir;

    /**
     * Agent ClassLoader，用于加载 libs 目录中的 JAR 及由 Bootstrap ClassLoader 委托的类
     */
    private final ClassLoader agentClassLoader;


    /**
     * 基于运行环境创建应用上下文
     *
     * <p>根据 Environment 中的配置自动确定基础目录、插件目录、配置目录和日志目录。
     * 当未指定应用名称时，使用 {@code conf/plugins/logs} 作为默认目录名；
     * 指定应用名称时，目录结构为 {@code <appName>/conf} 等
     *
     * @param environment 运行环境
     */
    public DefaultAppContext (Environment environment) {
        this.baseDir = environment.getAgentFile().getParentFile();
        this.appVersion = environment.getVersion();
        this.agentClassLoader = environment.getAgentClassLoader();

        if (StringUtils.isEmpty(environment.getAppName())) {
            this.appName = "";
            this.configDir = new File(baseDir, "conf");
            this.pluginDir = new File(baseDir, "plugins");
            this.logsDir = new File(baseDir, "logs");
        } else {
            this.appName = environment.getAppName();
            configDir = new File(baseDir, String.format("%s/conf", appName));
            pluginDir = new File(baseDir, String.format("%s/plugins", appName));
            logsDir = new File(baseDir, String.format("%s/logs", appName));
        }
        try {
            Files.createDirectories(configDir.toPath());
            Files.createDirectories(pluginDir.toPath());
        } catch (IOException ignored) {
        }
    }

    @Override
    public String getAppName() {
        return appName;
    }

    @Override
    public String getAppVersion() {
        return appVersion;
    }

    @Override
    public File getBaseDir() {
        return baseDir;
    }

    @Override
    public File getPluginDir() {
        return pluginDir;
    }

    @Override
    public File getConfigDir() {
        return configDir;
    }

    @Override
    public File getLogsDir() {
        return logsDir;
    }

    @Override
    public ClassLoader getAgentClassLoader() {
        return agentClassLoader;
    }

}
