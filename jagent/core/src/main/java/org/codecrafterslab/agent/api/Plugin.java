package org.codecrafterslab.agent.api;

import org.codecrafterslab.agent.logger.Logger;

import java.util.List;

/**
 * 插件接口，定义插件的基本属性和生命周期
 *
 * <p>实现此接口可通过 ServiceLoader 被自动发现和加载
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 */
public interface Plugin {

    /**
     * 获取插件名称
     *
     * @return 插件名称
     */
    String getName();

    /**
     * 获取插件使用的日志记录器
     *
     * @return 日志记录器实例
     */
    Logger getLogger();

    /**
     * 获取插件作者
     *
     * @return 插件作者
     */
    String getAuthor();

    /**
     * 获取插件版本
     *
     * @return 插件版本号
     */
    String getVersion();

    /**
     * 获取插件描述
     *
     * @return 插件描述信息
     */
    String getDescription();

    /**
     * 获取插件注册的字节码转换器列表
     *
     * @return 转换器列表
     */
    List<ITransformer> getTransformers();

    /**
     * 获取插件配置类
     *
     * @return 配置类类型
     */
    Class<? extends PluginConfiguration> getConfigurationClass();

    /**
     * 初始化插件
     *
     * @param appContext   应用上下文
     * @param pluginConfig 插件配置
     */
    default void init(AppContext appContext, PluginConfiguration pluginConfig) {
    }

}
