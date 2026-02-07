package org.codecrafterslab.agent.api;

import com.moandjiezana.toml.Toml;

public interface IConfigurable {

    /**
     * 加载应用配置
     *
     * @return Toml
     */
    Toml loadAppConfig();

    /**
     * 加载插件配置
     */
    Toml loadPluginConfig(String pluginName);
}
