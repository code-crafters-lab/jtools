package org.codecrafterslab.agent.api;

import com.moandjiezana.toml.Toml;

/**
 * 插件配置接口，定义插件配置的通用属性
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 */
public interface PluginConfiguration {

    /**
     * 获取原始 TOML 配置数据
     *
     * @return TOML 配置对象
     */
    Toml getTomlConfiguration();

    /**
     * 设置原始 TOML 配置数据
     *
     * @param toml TOML 配置对象
     */
    void setTomlConfiguration(Toml toml);

    /**
     * 插件是否被禁用
     *
     * @return true 表示插件已禁用
     */
    boolean isDisabled();

    /**
     * 配置校验，返回 false 时插件将不会被加载
     *
     * @return true 表示配置有效
     */
    default boolean validate() {
        return true;
    }

}
