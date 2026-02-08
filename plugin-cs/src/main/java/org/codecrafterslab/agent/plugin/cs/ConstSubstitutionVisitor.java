package org.codecrafterslab.agent.plugin.cs;

import org.codecrafterslab.agent.api.IMethodSupport;
import org.codecrafterslab.agent.core.asm.visitor.BaseClassVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.*;

public class ConstSubstitutionVisitor<T> extends BaseClassVisitor {

    private final ConstSubstitutionRule<T> rule;

    public ConstSubstitutionVisitor(ClassVisitor classVisitor, ConstSubstitutionRule<T> rule) {
        super(classVisitor);
        this.rule = rule;
    }

    @Override
    protected Optional<IMethodSupport> getMethodSupport() {
        return Optional.ofNullable(rule).map(ConstSubstitutionRule::getMethodInfo);
    }

    @Override
    protected MethodVisitor getModifyMethod(MethodVisitor original, int access, String name, String desc) {
        HashMap<T, T> ttHashMap = new HashMap<>();
        Map<T, T> map = Optional.ofNullable(rule).map(ConstSubstitutionRule::getReplacers)
                .map(replacers -> replacers.stream()
                        .reduce(ttHashMap,
                                (m, r) -> {
                                    m.put(r.getSrc(), r.getDst());
                                    return m;
                                },
                                (m1, m2) -> {
                                    m1.putAll(m2);
                                    return m1;
                                })
                )
                .orElse(ttHashMap);
        return new MethodConstantVisitor(original, map);
    }

    private class MethodConstantVisitor extends MethodVisitor {

        private final Map<Object, Object> replaceMap;

        public MethodConstantVisitor(int api, MethodVisitor methodVisitor, Map<T, T> replaceMap) {
            super(api, methodVisitor);
            this.replaceMap = new HashMap<>(replaceMap);
        }

        public MethodConstantVisitor(MethodVisitor methodVisitor, Map<T, T> replaceMap) {
            this(Opcodes.ASM9, methodVisitor, replaceMap);
        }

        /**
         * 访问常量加载指令（ldc）时触发（核心：替换字符串常量）
         *
         * @param value 常量值（这里对应我们要替换的字符串）
         */
        @Override
        public void visitLdcInsn(Object value) {
            if (replaceMap != null && replaceMap.containsKey(value)) {
                Object val = replaceMap.get(value);
                if (val != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("src value: {}", value);
                        log.debug("dst value: {}", val);
                    }
                    super.visitLdcInsn(val);
                    return;
                }
            }
            super.visitLdcInsn(value);
        }
    }

}
