package org.codecrafterslab.agent.core.plugin;

import lombok.extern.slf4j.Slf4j;
import org.codecrafterslab.agent.api.ITransformer;
import org.codecrafterslab.agent.api.Plugin;
import org.codecrafterslab.agent.api.PluginConfiguration;
import org.codecrafterslab.agent.utils.AgentUtil;

import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Stream;

@Slf4j
public abstract class BasePlugin implements Plugin {
    private static final Attributes.Name PLUGIN_NAME = new Attributes.Name("Plugin-Name");
    private static final Attributes.Name PLUGIN_NAME_DEFAULT = new Attributes.Name("Implementation-Title");
    private static final Attributes.Name PLUGIN_AUTHOR = new Attributes.Name("Plugin-Author");
    private static final Attributes.Name PLUGIN_AUTHOR_DEFAULT = new Attributes.Name("Built-By");
    private static final Attributes.Name PLUGIN_VERSION = new Attributes.Name("Plugin-Version");
    private static final Attributes.Name PLUGIN_VERSION_DEFAULT = new Attributes.Name("Implementation-Version");
    private static final Attributes.Name PLUGIN_DESCRIPTION = new Attributes.Name("Plugin-Description");

    private Manifest manifest;
    private String name;
    private String author;
    private String version;
    private String description;
    private final List<ITransformer> transformers = new ArrayList<>();
    private final Class<? extends PluginConfiguration> configurationClass;

    protected BasePlugin() {
        this(null);
    }

    protected BasePlugin(Class<? extends PluginConfiguration> configurationClass) {
        this.configurationClass = configurationClass;
        Manifest manifest = AgentUtil.getManifest(this.getClass());
        if (manifest == null) {
            throw new RuntimeException("Plugin manifest is missing.");
        }
        this.manifest = manifest;
        this.init(this.manifest);
    }

    @Override
    public Class<? extends PluginConfiguration> getConfigurationClass() {
        return configurationClass;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public List<ITransformer> getTransformers() {
        return transformers;
    }

    public <T extends ITransformer> void addTransformer(T transformer) {
        this.transformers.add(transformer);
    }

    public <T extends ITransformer> void addTransformers(List<T> transformers) {
        this.transformers.addAll(transformers);
    }

    protected void init(Manifest manifest) {
        this.manifest = manifest;
        Attributes attributes = manifest.getMainAttributes();
        this.name = getValue(attributes, PLUGIN_NAME, PLUGIN_NAME_DEFAULT, "Unknown");
        this.author = getValue(attributes, PLUGIN_AUTHOR, PLUGIN_AUTHOR_DEFAULT, "Unknown");
        this.version = getValue(attributes, PLUGIN_VERSION, PLUGIN_VERSION_DEFAULT, "Unknown");
        this.description = getValue(attributes, PLUGIN_DESCRIPTION, null, "");
    }

    protected String getValue(Attributes attributes,
                              Attributes.Name attributeName,
                              Attributes.Name defaultattributeName,
                              String defaultValue) {
        String value1 = Optional.ofNullable(attributeName).map(attributes::getValue).orElse("");
        String value2 = Optional.ofNullable(defaultattributeName).map(attributes::getValue).orElse("");
        return Stream.of(value1, value2, defaultValue)
                .filter(s -> s != null && !s.isEmpty()).findFirst().orElse("");
    }

}
