package org.codecrafterslab.agent.core.plugin;

import org.codecrafterslab.agent.api.AppContext;
import org.codecrafterslab.agent.api.Plugin;
import org.codecrafterslab.agent.core.Environment;
import org.codecrafterslab.agent.api.ITransformer;

import java.util.List;

@Deprecated
public interface PluginEntry extends Plugin {
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

    List<ITransformer> getTransformers();

}
