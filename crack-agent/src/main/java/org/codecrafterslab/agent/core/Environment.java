//package org.codecrafterslab.agent.core;
//
//
//import lombok.Data;
//import lombok.ToString;
//import org.codecrafterslab.agent.AgentUtil;
//
//import java.io.File;
//import java.lang.instrument.Instrumentation;
//
//@Data
//@ToString
//public final class Environment {
//    private final Instrumentation instrumentation;
//    private final String pid;
//    private final String version;
//    private final int versionNumber;
//    private final String appName;
//    private final File baseDir;
//    private final File agentFile;
//    private final File configDir;
//    private final File pluginsDir;
//    private final File logsDir;
////    private final String nativePrefix;
////    private final String disabledPluginSuffix;
////    private final boolean attachMode;
//
//    public Environment(Instrumentation instrumentation, File agentFile, boolean attachMode) {
//        this(instrumentation, agentFile, null, attachMode);
//    }
//
//    public Environment(Instrumentation instrumentation, File agentFile, String app, boolean attachMode) {
//        this.instrumentation = instrumentation;
//        this.agentFile = agentFile;
//        baseDir = agentFile.getParentFile();
//
//        if (StringUtils.isEmpty(app)) {
//            appName = "";
//            configDir = new File(baseDir, "config");
//            pluginsDir = new File(baseDir, "plugins");
//            logsDir = new File(baseDir, "logs");
//        } else {
//            appName = app.toLowerCase();
//            configDir = new File(baseDir, "config-" + appName);
//            pluginsDir = new File(baseDir, "plugins-" + appName);
//            logsDir = new File(baseDir, "logs-" + appName);
//        }
//
//        pid = AgentUtil.getProcessID();
//        version = Launcher.VERSION;
//        versionNumber = Launcher.VERSION_NUMBER;
////        nativePrefix = StringUtils.randomMethodName(15) + "_";
////        disabledPluginSuffix = ".disabled.jar";
////        this.attachMode = attachMode;
//    }
//
//}
