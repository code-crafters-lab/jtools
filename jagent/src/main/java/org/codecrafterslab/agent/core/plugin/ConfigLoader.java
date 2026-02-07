package org.codecrafterslab.agent.core.plugin;

import lombok.Getter;
import org.codecrafterslab.agent.api.AppContext;
import com.moandjiezana.toml.Toml;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Getter
public class ConfigLoader {
//    private final AppContext context;
//    private final Toml mainConfig;

//    public ConfigLoader(AppContext context) {
//        this.context = context;
//        this.mainConfig = loadConfig(context.getConfigDir(), "main");
//    }

    public static Toml loadConfig(File dir, String configName) {
        File configFile = new File(dir, String.format("%s.conf", configName));
        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(configFile.toPath()), StandardCharsets.UTF_8)) {
            return new Toml().read(reader);
        } catch (Exception e) {
            return new Toml();
        }
    }

//    /**
//     * 加载插件配置
//     *
//     * @param pluginName 插件名
//     * @return 插件配置
//     */
//    public static Toml loadPluginConfig(String pluginName) {
//        return loadConfig(context.getPluginDir(), pluginName);
//    }

}
