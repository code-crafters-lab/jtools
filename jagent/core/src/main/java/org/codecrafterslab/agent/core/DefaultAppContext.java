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
     * 插件 ClassLoader，用于加载插件 JAR 中由 Bootstrap ClassLoader 委托的类
     */
    private final ClassLoader pluginClassLoader;

    /**
     * 创建默认上下文，使用默认目录结构
     *
     * @param agentFile Agent JAR 文件
     */
    public DefaultAppContext(File agentFile) {
        this(agentFile, null, null);
    }

    /**
     * 创建应用上下文
     *
     * @param agentFile   Agent JAR 文件
     * @param appName     应用名称，为空时使用默认目录
     * @param appVersion  应用版本
     */
    public DefaultAppContext(File agentFile, String appName, String appVersion) {
        this(agentFile, appName, appVersion, null);
    }

    /**
     * 创建应用上下文（含插件 ClassLoader）
     *
     * @param agentFile         Agent JAR 文件
     * @param appName           应用名称，为空时使用默认目录
     * @param appVersion        应用版本
     * @param pluginClassLoader 插件 ClassLoader，可为 {@code null}
     */
    public DefaultAppContext(File agentFile, String appName, String appVersion, ClassLoader pluginClassLoader) {
        this.baseDir = agentFile.getParentFile();
        this.appVersion = appVersion;
        this.pluginClassLoader = pluginClassLoader;

        if (StringUtils.isEmpty(appName)) {
            this.appName = "";
            this.configDir = new File(baseDir, "conf");
            this.pluginDir = new File(baseDir, "plugins");
            this.logsDir = new File(baseDir, "logs");
        } else {
            this.appName = appName;
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
    public ClassLoader getPluginClassLoader() {
        return pluginClassLoader;
    }

}
