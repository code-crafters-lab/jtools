package org.codecrafterslab.agent.plugin.cs;

import com.google.auto.service.AutoService;
import org.codecrafterslab.agent.api.*;
import org.codecrafterslab.agent.core.plugin.BasePlugin;

import java.util.*;
import java.util.stream.Collectors;

@AutoService(Plugin.class)
public class ConstSubstitutionPlugin extends BasePlugin implements Plugin {
    private final List<ITransformer> transformers = new ArrayList<>();

    public ConstSubstitutionPlugin() {
        super(ConstSubstitutionPluginConfiguration.class);
    }

    @Override
    public void init(AppContext appContext, PluginConfiguration pluginConfig) {
        if (pluginConfig instanceof ConstSubstitutionPluginConfiguration) {
            Optional.ofNullable(((ConstSubstitutionPluginConfiguration) pluginConfig).getRules())
                    .map(Collection::stream)
                    .map(rules -> rules.map(ConstSubstitutionTransformer::new).collect(Collectors.toList()))
                    .ifPresent(transformers::addAll);
        }
    }

    @Override
    public List<ITransformer> getTransformers() {
        return transformers;
    }

}
