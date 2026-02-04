package org.codecrafterslab.agent.core.plugin;

import com.janetfilter.core.models.FilterRule;
import lombok.Getter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class PluginConfig {
    private final File file;
    private final Map<String, List<FilterRule>> data;

    public PluginConfig(File file, Map<String, List<FilterRule>> data) {
        this.file = file;
        this.data = data;
    }

    public List<FilterRule> getBySection(String section) {
        return data.getOrDefault(section, new ArrayList<>());
    }

}
