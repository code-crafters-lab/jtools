package org.codecrafterslab.agent.api;

import java.io.File;
import java.nio.file.Path;

/**
 * 应用上下文接口，封装应用目录结构和元信息
 *
 * <p>提供基础目录、插件目录、配置目录和日志目录的统一访问入口
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 */
public interface AppContext {

    /**
     * 获取应用基础目录的 Path 对象
     *
     * @return 应用基础目录的 Path
     */
    default Path getRootDir() {
        return getBaseDir().toPath();
    }

    /**
     * 获取应用名称
     *
     * @return 应用名称
     */
    String getAppName();

    /**
     * 获取应用版本
     *
     * @return 应用版本号
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
     * @return 插件所在目录
     */
    File getPluginDir();

    /**
     * 获取配置目录
     *
     * @return 配置所在目录
     */
    File getConfigDir();

    /**
     * 获取日志目录
     *
     * @return 日志所在目录
     */
    File getLogsDir();

}

