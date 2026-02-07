package org.codecrafterslab.agent.api;


import com.moandjiezana.toml.Toml;

import java.util.List;

public interface Plugin {

    /**
     * 初始化插件
     *
     * @param appContext   应用上下文
     * @param pluginConfig 插件配置
     */
    default void init(AppContext appContext, Toml pluginConfig) {
    }

    /**
     * 获取插件名称
     *
     * @return 名称
     */
    String getName();

    /**
     * 获取插件作者
     *
     * @return 作者
     */
    String getAuthor();

    /**
     * 获取插件版本
     *
     * @return 版本
     */
    String getVersion();

    /**
     * 获取插件描述
     *
     * @return 描述
     */
    String getDescription();

    /**
     * 获取插件转换器
     *
     * @return 转换器
     */
    List<ITransformer> getTransformers();

}
