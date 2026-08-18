package org.codecrafterslab.agent.logger;


/**
 * 日志记录器工厂接口
 *
 * <p>抽象日志记录器的创建方式，通过统一的工厂入口按名称获取日志记录器实例，
 * 便于不同的实现方（如控制台、文件输出）提供各自的创建逻辑
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time 2020/04/29 14:56
 */
public interface ILoggerFactory {
    /**
     * 获取日志记录器
     *
     * @param name 记录器使用名称
     * @return Logger 日志记录器实例
     */
    Logger getLogger(String name);
}
