package org.codecrafterslab.agent.core;

import com.janetfilter.core.Launcher;
import com.janetfilter.core.utils.ProcessUtils;
import com.janetfilter.core.utils.StringUtils;
import lombok.Getter;
import lombok.ToString;

import java.io.File;
import java.lang.instrument.Instrumentation;

@Getter
@ToString
public final class Environment {
    private final Instrumentation instrumentation;
    private final File agentFile;
    private final boolean attachMode;

    private final File baseDir;
    private final File configDir;
    private final File pluginsDir;
    private final File logsDir;

    private final String pid;
    private final String version;
    private final int versionNumber;
    private final String appName;
    private final String nativePrefix;
    private final String disabledPluginSuffix;


    public Environment(Instrumentation instrumentation, File agentFile, boolean attachMode) {
        this(instrumentation, agentFile, null, attachMode);
    }

    public Environment(Instrumentation instrumentation, File agentFile, String app, boolean attachMode) {
        this.instrumentation = instrumentation;
        this.agentFile = agentFile;
        this.attachMode = attachMode;
        this.baseDir = agentFile.getParentFile();

        if (StringUtils.isEmpty(app)) {
            this.appName = "";
            this.configDir = new File(baseDir, "conf");
            this.pluginsDir = new File(baseDir, "plugins");
            this.logsDir = new File(baseDir, "logs");
        } else {
            appName = app;
            configDir = new File(baseDir, String.format("%s/conf", appName));
            pluginsDir = new File(baseDir, String.format("%s/plugins", appName));
            logsDir = new File(baseDir, String.format("%s/logs", appName));
        }

        this.pid = ProcessUtils.currentId();
        this.version = Launcher.VERSION;
        this.versionNumber = Launcher.VERSION_NUMBER;
        this.nativePrefix = StringUtils.randomMethodName(15) + "_";
        this.disabledPluginSuffix = ".disabled.jar";

    }

    public boolean isJavaagentMode() {
        return !attachMode;
    }

}
