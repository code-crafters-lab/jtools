package org.codecrafterslab.agent.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 方法匹配接口，用于判断方法是否符合转换条件
 *
 * <p>实现此接口可自定义方法筛选规则，
 * 如按访问修饰符、名称、描述符等匹配
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 */
@FunctionalInterface
public interface IMethodSupport {

    /**
     * 判断指定方法是否支持修改
     *
     * @param access     方法访问修饰符
     * @param name       方法名称
     * @param descriptor 方法描述符
     * @param signature  方法签名
     * @param exceptions 方法抛出的异常列表
     * @return true 表示匹配该方法
     */
    boolean accept(int access, String name, String descriptor, String signature, String[] exceptions);

    /**
     * 默认不支持任何方法
     */
    IMethodSupport DEFAULT = (access, name, descriptor, signature, exceptions) -> false;

    /**
     * 方法信息实体，通过精确匹配实现方法筛选
     *
     * @author Wu Yujie
     * @email coffee377@dingtalk.com
     */
    @Data
    @Builder
    @EqualsAndHashCode
    @AllArgsConstructor
    class MethodInfo implements IMethodSupport {

        /**
         * 方法访问修饰符
         */
        private int access;

        /**
         * 方法名称
         */
        private String name;

        /**
         * 方法描述符
         */
        private String descriptor;

        /**
         * 方法签名
         */
        private String signature;

        /**
         * 方法抛出的异常列表
         */
        private String[] exceptions;

        @Override
        public boolean accept(int access, String name, String descriptor, String signature, String[] exceptions) {
            return MethodInfo.builder().access(access).name(name)
                    .descriptor(descriptor).signature(signature).exceptions(exceptions).build()
                    .equals(this);
        }
    }

}
