package org.codecrafterslab.agent.api;

import java.io.File;

public interface AppContext extends IConfigurable {

    /**
     * 获取应用名
     *
     * @return 应用名
     */
    String getAppName();

    /**
     * 获取应用版本
     *
     * @return 应用版本
     */
    String getAppVersion();

    /**
     * 获取基础目录
     *
     * @return 应用基础目录
     */
    File getBaseDir();

    /**
     * 获取插件目录
     *
     * @return 插件目录
     */
    File getPluginDir();

    /**
     * 获取配置目录
     *
     * @return 配置目录
     */
    File getConfigDir();

    /**
     * 获取日志目录
     *
     * @return 日志目录
     */
    File getLogsDir();

}

