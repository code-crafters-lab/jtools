package org.codecrafterslab.agent.core;


import java.security.ProtectionDomain;

/**
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time 2019/08/02 15:16
 */
public interface Transformer {

    /**
     * 获取java语言规范定义的格式输出，如：com.voc.core.action.CallbackAction
     *
     * @return String
     */
    String getCanonicalName();

    /**
     * the fully canonical name of the desired class, like this: package/to/className.
     *
     * @return String
     */
    default String getHookClassName() {
        return getCanonicalName().replace('.', '/');
    }

    /**
     * whether to load in attach mode
     */
    default boolean attachMode() {
        return true;
    }

    /**
     * whether to load in -javaagent mode
     */
    default boolean javaagentMode() {
        return true;
    }

    /**
     * for normal transformers only
     */
    default byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classBytes, int order) {
        try {
            return getCode(classBytes, order);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * byte code
     *
     * @param sourceBytes byte[]
     * @return byte[]
     */
    default byte[] getCode(byte[] sourceBytes, int order) throws Exception {
        return sourceBytes;
    }

}
