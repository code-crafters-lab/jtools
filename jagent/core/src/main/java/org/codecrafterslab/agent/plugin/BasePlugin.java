package org.codecrafterslab.agent.plugin;

import org.codecrafterslab.agent.api.ITransformer;
import org.codecrafterslab.agent.api.Plugin;
import org.codecrafterslab.agent.api.PluginConfiguration;
import org.codecrafterslab.agent.logger.Logger;
import org.codecrafterslab.agent.logger.impl.LoggerFactory;
import org.codecrafterslab.agent.utils.AgentUtils;

import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Stream;

/**
 * 插件基类，提供插件元数据管理与转换器注册功能
 *
 * <p>从 JAR 的 MANIFEST.MF 自动读取插件名称、作者、版本等信息，
 * 支持命名空间回退（Plugin-Name -> Implementation-Title）
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 */
public abstract class BasePlugin implements Plugin {

    /**
     * 插件名称 Manifest 属性名
     */
    private static final Attributes.Name PLUGIN_NAME = new Attributes.Name("Plugin-Name");

    /**
     * 插件名称回退属性名
     */
    private static final Attributes.Name PLUGIN_NAME_DEFAULT = new Attributes.Name("Implementation-Title");

    /**
     * 插件作者 Manifest 属性名
     */
    private static final Attributes.Name PLUGIN_AUTHOR = new Attributes.Name("Plugin-Author");

    /**
     * 插件作者回退属性名
     */
    private static final Attributes.Name PLUGIN_AUTHOR_DEFAULT = new Attributes.Name("Built-By");

    /**
     * 插件版本 Manifest 属性名
     */
    private static final Attributes.Name PLUGIN_VERSION = new Attributes.Name("Plugin-Version");

    /**
     * 插件版本回退属性名
     */
    private static final Attributes.Name PLUGIN_VERSION_DEFAULT = new Attributes.Name("Implementation-Version");

    /**
     * 插件描述 Manifest 属性名
     */
    private static final Attributes.Name PLUGIN_DESCRIPTION = new Attributes.Name("Plugin-Description");

    /**
     * JAR 清单文件
     */
    private Manifest manifest;

    /**
     * 插件名称
     */
    private String name;

    /**
     * 插件作者
     */
    private String author;

    /**
     * 插件版本
     */
    private String version;

    /**
     * 插件描述
     */
    private String description;

    /**
     * 插件注册的转换器列表
     */
    private final List<ITransformer> transformers = new ArrayList<>();

    /**
     * 插件配置类
     */
    private final Class<? extends PluginConfiguration> configurationClass;

    /**
     * 使用默认配置类创建插件
     */
    protected BasePlugin() {
        this(null);
    }

    /**
     * 创建插件，指定配置类
     *
     * @param configurationClass 插件配置类
     */
    protected BasePlugin(Class<? extends PluginConfiguration> configurationClass) {
        this.configurationClass = configurationClass;
        Manifest manifest = AgentUtils.getManifest(this.getClass());
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
    public Logger getLogger() {
        return LoggerFactory.getLogger(getName());
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

    /**
     * 添加单个转换器到插件
     *
     * @param transformer 转换器实例
     */
    public <T extends ITransformer> void addTransformer(T transformer) {
        this.transformers.add(transformer);
    }

    /**
     * 批量添加转换器到插件
     *
     * @param transformers 转换器列表
     */
    public <T extends ITransformer> void addTransformers(List<T> transformers) {
        this.transformers.addAll(transformers);
    }

    /**
     * 从 Manifest 读取插件元数据
     *
     * @param manifest JAR 清单
     */
    protected void init(Manifest manifest) {
        this.manifest = manifest;
        Attributes attributes = manifest.getMainAttributes();
        this.name = getValue(attributes, PLUGIN_NAME, PLUGIN_NAME_DEFAULT, "Unknown");
        this.author = getValue(attributes, PLUGIN_AUTHOR, PLUGIN_AUTHOR_DEFAULT, "Unknown");
        this.version = getValue(attributes, PLUGIN_VERSION, PLUGIN_VERSION_DEFAULT, "Unknown");
        this.description = getValue(attributes, PLUGIN_DESCRIPTION, null, "");
    }

    /**
     * 从 Manifest 属性中读取值，支持回退查找
     *
     * @param attributes           Manifest 属性集
     * @param attributeName        主属性名
     * @param defaultattributeName 回退属性名
     * @param defaultValue         默认值
     * @return 属性值，优先级：主属性 > 回退属性 > 默认值
     */
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
