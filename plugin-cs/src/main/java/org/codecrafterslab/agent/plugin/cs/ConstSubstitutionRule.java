package org.codecrafterslab.agent.plugin.cs;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.codecrafterslab.agent.api.IMethodSupport;

import java.util.List;

@Data
public class ConstSubstitutionRule<T> {
    /**
     * 类名
     */
    private String className;
    /**
     * 支持修改的方法信息
     */
    private IMethodSupport.MethodInfo methodInfo;
    /**
     * 常量替换 KV
     */
    private List<Replacer<T>> replacers;

    @Data
    @AllArgsConstructor
    static class Replacer<T> {
        private T src;
        private T dst;
    }
}
