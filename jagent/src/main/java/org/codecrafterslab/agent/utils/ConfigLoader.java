package org.codecrafterslab.agent.utils;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.moandjiezana.toml.Toml;
import lombok.extern.slf4j.Slf4j;
import org.codecrafterslab.agent.api.PluginConfiguration;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class ConfigLoader {

    public static Toml load(Path configPath) {
        Toml toml = new Toml();
        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(configPath), StandardCharsets.UTF_8)) {
            toml.read(reader);
        } catch (Exception ignored) {
            log.warn("Failed to load config file: {}", configPath);
        }
        return toml;
    }

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

    public static Toml load(File dir, String configName) {
        File configFile = new File(dir, configName);
        return load(configFile.toPath());
    }

    public static <T extends PluginConfiguration> T load(File dir, String configName, Class<T> tClass) {
        if (tClass == null) return null;
        File configFile = new File(dir, configName);
        return load(configFile.toPath(), tClass);
    }

}
