package org.codecrafterslab.agent.core.plugin;

import com.moandjiezana.toml.Toml;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codecrafterslab.agent.api.PluginConfiguration;

@Data
@NoArgsConstructor
public class BasePluginConfiguration implements PluginConfiguration {

    private Toml tomlConfiguration;

    /**
     * Whether the plugin is disabled or not
     */
    private boolean disabled = false;
}
