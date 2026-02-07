package org.codecrafterslab.agent.core;

import com.janetfilter.core.utils.StringUtils;
import com.moandjiezana.toml.Toml;
import org.codecrafterslab.agent.api.AppContext;
import org.codecrafterslab.agent.core.plugin.ConfigLoader;

import java.io.File;

public class DefaultAppContext implements AppContext {
    private final String appName;
    private final String appVersion;

    private final File baseDir;
    private final File pluginDir;
    private final File configDir;
    private final File logsDir;

    public DefaultAppContext(File agentFile) {
        this(agentFile, null, null);
    }

    public DefaultAppContext(File agentFile, String appName, String appVersion) {
        this.baseDir = agentFile.getParentFile();
        this.appVersion = appVersion;

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
        this.pluginDir.mkdirs();
        this.configDir.mkdirs();
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
    public Toml loadAppConfig() {
        return ConfigLoader.loadConfig(pluginDir.getParentFile(), appName);
    }

    @Override
    public Toml loadPluginConfig(String pluginName) {
        return ConfigLoader.loadConfig(pluginDir, pluginName);
    }

}
