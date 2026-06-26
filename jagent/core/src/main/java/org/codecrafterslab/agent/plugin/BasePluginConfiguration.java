package org.codecrafterslab.agent.plugin;

import com.moandjiezana.toml.Toml;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codecrafterslab.agent.api.PluginConfiguration;

/**
 * 插件配置基类，提供通用配置功能
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 */
@Data
@NoArgsConstructor
public class BasePluginConfiguration implements PluginConfiguration {

    /**
     * TOML 配置原始数据
     */
    private Toml tomlConfiguration;

    /**
     * 插件是否被禁用
     */
    private boolean disabled = false;
}
