package org.codecrafterslab.agent.core.plugin;

import lombok.extern.slf4j.Slf4j;
import org.codecrafterslab.agent.Agent;
import org.codecrafterslab.agent.Initializer;
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

/**
 * 插件管理器，负责扫描、加载和初始化插件
 *
 * <p>通过 ServiceLoader 发现插件，自动加载插件配置，
 * 并将插件的转换器注册到 Agent 中
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 */
@Slf4j
public final class PluginManager {

    /**
     * 已加载的插件列表
     */
    private static final List<Plugin> plugins = new ArrayList<>();

    /**
     * 从插件目录加载所有插件
     *
     * <p>流程：
     * - 扫描插件目录下所有 JAR 文件
     * - 创建独立 ClassLoader 加载插件
     * - 通过 ServiceLoader 发现 Plugin 实现
     * - 读取插件配置，初始化并注册转换器
     *
     * @param agent      Agent 实例
     * @param appContext 应用上下文
     */
    public static void loadPlugins(Agent agent, AppContext appContext) {
        Instant startTime = Instant.now();
        try {
            File pluginDir = appContext.getPluginDir();
            if (!pluginDir.exists() || !pluginDir.isDirectory()) return;
            URL[] urls = Initializer.scanJarUrls(pluginDir);

            ClassLoader parent = appContext.getAgentClassLoader() != null
                ? appContext.getAgentClassLoader()
                : Thread.currentThread().getContextClassLoader();
            ClassLoader pluginClassLoader = new URLClassLoader(urls, parent);
            ServiceLoader<Plugin> loader = ServiceLoader.load(Plugin.class, pluginClassLoader);

            for (Plugin plugin : loader) {
                // TODO 读取插件独立配置,不应为空，未读取的配置就使用默认配置
                PluginConfiguration pluginConfig = ConfigLoader.load(
                        appContext.getConfigDir(),
                        String.format("%s.%s", plugin.getName(), "toml"),
                        plugin.getConfigurationClass()
                );
                if (pluginConfig != null && pluginConfig.isDisabled()) {
                    continue;
                }
                plugin.init(appContext, pluginConfig);
                List<ITransformer> transformers = Optional.ofNullable(plugin.getTransformers()).orElse(new ArrayList<>());
                agent.addTransformers(transformers);
                plugins.add(plugin);
            }

            if (log.isDebugEnabled() && !plugins.isEmpty()) {
                Duration duration = Duration.between(startTime, Instant.now());
                String elapsedTime = getElapsedTime(duration);
                log.debug("{} plugins loaded, {} elapsed", plugins.size(), elapsedTime);
            }
        } catch (Exception e) {
            log.error("Failed to load plugins", e);
        }
    }

    /**
     * 将耗时转换为可读格式
     *
     * @param duration 耗时
     * @return 格式化后的时间字符串，如 1.5s、800ms、1.5min
     */
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
