package org.codecrafterslab.agent.core.plugin;

import org.codecrafterslab.agent.core.Environment;
import org.codecrafterslab.agent.core.Transformer;

import java.util.List;

public interface PluginEntry {
    default void init(Environment environment, PluginConfig config) {
    }

    String getName();

    String getAuthor();

    default String getVersion() {
        return "v1.0.0";
    }

    default String getDescription() {
        return "A JAgent plugin.";
    }

    List<Transformer> getTransformers();
}
