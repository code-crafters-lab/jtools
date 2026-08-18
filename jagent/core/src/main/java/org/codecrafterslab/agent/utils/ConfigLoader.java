package org.codecrafterslab.agent.utils;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.moandjiezana.toml.Toml;
import org.codecrafterslab.agent.api.PluginConfiguration;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 配置加载器，支持加载 TOML 配置文件并反序列化为插件配置对象
 *
 * <p>字段命名策略为 lowercase-with-dashes，
 * 如 disabled 对应 TOML 中的 disabled
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 */
public class ConfigLoader {

    /**
     * 从指定路径加载 TOML 配置
     *
     * @param configPath 配置文件路径
     * @return TOML 配置对象
     */
    public static Toml load(Path configPath) {
        Toml toml = new Toml();
        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(configPath), StandardCharsets.UTF_8)) {
            toml.read(reader);
        } catch (Exception ignored) {
            // log.warn("Failed to load config file: {}", configPath);
        }
        return toml;
    }

    /**
     * 加载配置并反序列化为指定配置类
     *
     * @param path    配置文件路径
     * @param tClass  配置类类型
     * @param <T>     配置类
     * @return 配置对象，同时注入原始 TOML 数据
     */
    public static <T extends PluginConfiguration> T load(Path path, Class<T> tClass) {
        if (tClass == null) return null;
        Toml toml = load(path);
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
                .create();

        JsonElement json = gson.toJsonTree(toml.toMap());
        T t = gson.fromJson(json, tClass);
        t.setTomlConfiguration(toml);
        return t;
    }

    /**
     * 从目录下加载 TOML 配置文件
     *
     * @param dir        配置目录
     * @param configName 配置文件名
     * @return TOML 配置对象
     */
    public static Toml load(File dir, String configName) {
        File configFile = new File(dir, configName);
        return load(configFile.toPath());
    }

    /**
     * 从目录下加载配置并反序列化
     *
     * @param dir        配置目录
     * @param configName 配置文件名
     * @param tClass     配置类类型
     * @param <T>        配置类
     * @return 配置对象
     */
    public static <T extends PluginConfiguration> T load(File dir, String configName, Class<T> tClass) {
        if (tClass == null) return null;
        File configFile = new File(dir, configName);
        return load(configFile.toPath(), tClass);
    }

}
