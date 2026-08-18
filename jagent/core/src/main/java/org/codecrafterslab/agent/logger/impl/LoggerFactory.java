package org.codecrafterslab.agent.logger.impl;

import org.codecrafterslab.agent.logger.ILoggerFactory;
import org.codecrafterslab.agent.logger.Logger;

/**
 * 日志记录器工厂
 *
 * <p>提供按类或名称获取日志记录器的静态入口，内部委托给
 * {@link ILoggerFactory} 完成实际创建，默认使用 {@link InnerLogger} 实现
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time 2020/04/29 14:54
 */
public class LoggerFactory {

    /**
     * 按类获取日志记录器
     *
     * @param clazz 目标类，使用其全限定类名作为日志名称
     * @return 对应的日志记录器
     */
    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getCanonicalName());
    }

    /**
     * 按名称获取日志记录器
     *
     * @param name 日志记录器名称
     * @return 对应的日志记录器
     */
    public static Logger getLogger(String name) {
        ILoggerFactory loggerFactory = getLoggerFactory();
        return loggerFactory.getLogger(name);
    }

    /**
     * 获取日志记录器工厂实例
     *
     * @return 使用 {@link InnerLogger} 构造方法的工厂实例
     */
    private static ILoggerFactory getLoggerFactory() {
        return InnerLogger::new;
    }

}
