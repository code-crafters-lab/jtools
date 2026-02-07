package org.codecrafterslab.agent.plugin.cs;

import lombok.Data;
import org.codecrafterslab.agent.api.IMethodSupport;

import java.util.Map;

@Data
public class ConstSubstitutionRule<T> {
    /**
     * 类名
     */
    private String className;
    /**
     * 支持修改的方法
     */
    private IMethodSupport methodSupport;
    /**
     * 常量替换 KV
     */
    private Map<T, T> constantMap;
}
