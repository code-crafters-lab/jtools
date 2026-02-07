package org.codecrafterslab.agent.api;

@FunctionalInterface
public interface IMethodSupport {

    /**
     * 指定方法是否支持修改
     *
     * @param access     修饰符
     * @param name       名称
     * @param desc       描述
     * @param signature  签名
     * @param exceptions 异常
     * @return boolean
     */
    boolean accept(int access, String name, String desc, String signature, String[] exceptions);

    /**
     * 默认不支持
     */
    IMethodSupport DEFAULT = (access, name, desc, signature, exceptions) -> false;

}
