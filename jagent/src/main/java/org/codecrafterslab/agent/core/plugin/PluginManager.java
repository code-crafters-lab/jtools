package org.codecrafterslab.agent.core.plugin;

import lombok.extern.slf4j.Slf4j;
import org.codecrafterslab.agent.Agent;
import org.codecrafterslab.agent.api.AppContext;
import org.codecrafterslab.agent.api.ITransformer;
import org.codecrafterslab.agent.api.Plugin;
import org.codecrafterslab.agent.api.PluginConfiguration;
import org.codecrafterslab.agent.utils.ConfigLoader;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Slf4j
public final class PluginManager {
    private static final List<Plugin> plugins = new ArrayList<>();
//    private final Instrumentation inst;
//    private final Agent agent;
//    private final Environment environment;
//    public PluginManager(Agent agent, Environment environment) {
//        this.inst = environment.getInstrumentation();
//        this.agent = agent;
//        this.environment = environment;
//    }

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
                // TODO 可在这里读取插件独立配置
                PluginConfiguration pluginConfig = ConfigLoader.load(
                        appContext.getConfigDir(),
                        String.format("%s.%s", plugin.getName(), "toml"),
                        plugin.getConfigurationClass()
                );
                plugin.init(appContext, pluginConfig);
                List<ITransformer> transformers = Optional.ofNullable(plugin.getTransformers()).orElse(new ArrayList<>());
                agent.addTransformers(transformers);
                plugins.add(plugin);
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

}
