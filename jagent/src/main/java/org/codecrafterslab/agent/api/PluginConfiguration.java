package org.codecrafterslab.agent.api;

import com.moandjiezana.toml.Toml;

public interface PluginConfiguration {

    /**
     * 获取配置文件
     */
    Toml getTomlConfiguration();

    void setTomlConfiguration(Toml toml);

    /**
     * 插件是否禁用
     */
    boolean isDisabled();

    /**
     * 配置校验（可选实现，默认返回 true）
     */
    default boolean validate() {
        return true;
    }

}
