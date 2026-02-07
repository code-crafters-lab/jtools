package org.codecrafterslab.agent.core.plugin;

import com.moandjiezana.toml.Toml;
import lombok.extern.slf4j.Slf4j;
import org.codecrafterslab.agent.Agent;
import org.codecrafterslab.agent.api.AppContext;
import org.codecrafterslab.agent.api.ITransformer;
import org.codecrafterslab.agent.api.Plugin;
import org.codecrafterslab.agent.core.Environment;
import com.janetfilter.core.commons.ConfigParser;
import com.janetfilter.core.commons.DebugInfo;
import com.janetfilter.core.utils.StringUtils;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

@Slf4j
public final class PluginManager {
    private static final String ENTRY_NAME = "JANF-Plugin-Entry";
    private static final List<Plugin> plugins = new ArrayList<>();
    private final Instrumentation inst;
    private final Agent agent;
    private final Environment environment;

    public PluginManager(Agent agent, Environment environment) {
        this.inst = environment.getInstrumentation();
        this.agent = agent;
        this.environment = environment;
    }

    public static void loadPlugins(Agent agent, AppContext appContext) {
        Instant startTime = Instant.now();
        try {
            File pluginDir = appContext.getPluginDir();
            if (!pluginDir.exists() || !pluginDir.isDirectory()) return;
            File[] pluginFiles = pluginDir.listFiles((dir, name) -> name.endsWith(".jar"));
            if (null == pluginFiles) {
                return;
            }

            URL[] urls = Arrays.stream(pluginFiles)
                    .map(file -> {
                        try {
                            return file.toURI().toURL();
                        } catch (Exception e) {
                            return null;
                        }
                    }).filter(Objects::nonNull).toArray(URL[]::new);
            ClassLoader pluginClassLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
            ServiceLoader<Plugin> loader = ServiceLoader.load(Plugin.class, pluginClassLoader);

            for (Plugin plugin : loader) {
                // 可在这里读取插件独立配置
                Toml pluginConfig = appContext.loadPluginConfig(plugin.getName());
                // TODO: toml 转换成 插件事件配置实体，或者统一插件配置基础接口
                plugin.init(appContext, pluginConfig);
                plugins.add(plugin);
                List<ITransformer> transformers = Optional.ofNullable(plugin.getTransformers()).orElse(new ArrayList<>());
                agent.addTransformers(transformers);
            }

            if (log.isInfoEnabled() && !plugins.isEmpty()) {
                Duration duration = Duration.between(startTime, Instant.now());
                String elapsedTime = getElapsedTime(duration);
                log.info("{} plugins loaded, {} elapsed", plugins.size(), elapsedTime);
            }
        } catch (Exception e) {
            log.error("Failed to load plugins", e);
        }
    }

    private static String getElapsedTime(Duration duration) {
        long millis = duration.toMillis(); // 获取总毫秒数，作为判断依据
        // 核心逻辑：根据耗时是否超过1秒，选择不同的展示格式
        String elapsedTime;
        if (millis > 1000 * 59) {
            // 超过59秒：转成分钟，保留一位小数（如 1.5min）
            double minutes = millis / (1000.0 * 60);
            elapsedTime = String.format("%.1fmin", minutes);
        } else if (millis > 1000) {
            // 超过1秒小于 59 秒：转成秒，保留一位小数（如 1.5s）
            double seconds = millis / 1000.0;
            elapsedTime = String.format("%.1fs", seconds);
        } else {
            // ≤1秒：直接展示毫秒（如 800ms）
            elapsedTime = String.format("%dms", millis);
        }
        return elapsedTime;
    }

    @Deprecated
    public void loadPlugins() {
        long startTime = System.currentTimeMillis();

        File pluginsDirectory = environment.getPluginsDir();
        if (!pluginsDirectory.exists() || !pluginsDirectory.isDirectory()) {
            return;
        }

        File[] pluginFiles = pluginsDirectory.listFiles((d, n) -> n.endsWith(".jar"));
        if (null == pluginFiles) {
            return;
        }

        try {
            ExecutorService executorService = Executors.newCachedThreadPool();
            for (File pluginFile : pluginFiles) {
                executorService.submit(new PluginLoadTask(pluginFile));
            }

            executorService.shutdown();
            if (!executorService.awaitTermination(30L, TimeUnit.SECONDS)) {
                throw new RuntimeException("Load plugin timeout");
            }

            DebugInfo.debug(String.format("============ All plugins loaded, %.2fs elapsed ============", (System.currentTimeMillis() - startTime) / 1000D));
        } catch (Throwable e) {
            DebugInfo.error("Load plugin failed", e);
        }
    }

    @Deprecated
    private class PluginLoadTask implements Runnable {
        private final File pluginFile;

        public PluginLoadTask(File pluginFile) {
            this.pluginFile = pluginFile;
        }

        @Override
        public void run() {
            try {
                if (pluginFile.getName().endsWith(environment.getDisabledPluginSuffix())) {
                    DebugInfo.debug("Disabled plugin: " + pluginFile + ", ignored.");
                    return;
                }

                JarFile jarFile = new JarFile(pluginFile);
                Manifest manifest = jarFile.getManifest();
                String entryClass = manifest.getMainAttributes().getValue(ENTRY_NAME);
                if (StringUtils.isEmpty(entryClass)) {
                    return;
                }

                PluginClassLoader classLoader = new PluginClassLoader(jarFile);
                Class<?> klass = Class.forName(entryClass, false, classLoader);
                if (!Arrays.asList(klass.getInterfaces()).contains(PluginEntry.class)) {
                    return;
                }

                synchronized (inst) {
                    inst.appendToSystemClassLoaderSearch(jarFile);
                }

                PluginEntry pluginEntry = (PluginEntry) Class.forName(entryClass).newInstance();

                File configFile = new File(environment.getConfigDir(), pluginEntry.getName().toLowerCase() + ".conf");
                PluginConfig pluginConfig = new PluginConfig(configFile, ConfigParser.parse(configFile));
                pluginEntry.init(environment, pluginConfig);

                agent.addTransformers(pluginEntry.getTransformers());

                DebugInfo.debug("Plugin loaded: {name=" + pluginEntry.getName() + ", version=" + pluginEntry.getVersion() + ", author=" + pluginEntry.getAuthor() + "}");
            } catch (Throwable e) {
                DebugInfo.error("Parse plugin info failed", e);
            }
        }
    }
}
