package org.codecrafterslab.agent.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@FunctionalInterface
public interface IMethodSupport {

    /**
     * 指定方法是否支持修改
     *
     * @param access     修饰符
     * @param name       名称
     * @param descriptor 描述
     * @param signature  签名
     * @param exceptions 异常
     * @return boolean
     */
    boolean accept(int access, String name, String descriptor, String signature, String[] exceptions);

    /**
     * 默认不支持
     */
    IMethodSupport DEFAULT = (access, name, descriptor, signature, exceptions) -> false;

    @Data
    @Builder
    @EqualsAndHashCode
    @AllArgsConstructor
    class MethodInfo implements IMethodSupport {
        private int access;
        private String name;
        private String descriptor;
        private String signature;
        private String[] exceptions;

        @Override
        public boolean accept(int access, String name, String descriptor, String signature, String[] exceptions) {
            return MethodInfo.builder().access(access).name(name)
                    .descriptor(descriptor).signature(signature).exceptions(exceptions).build()
                    .equals(this);
        }
    }

}
