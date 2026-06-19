package org.codecrafterslab.agent.plugin.cs;

import lombok.extern.slf4j.Slf4j;
import org.codecrafterslab.agent.core.asm.ASMTransformer;
import org.objectweb.asm.*;

import java.util.Optional;

@Slf4j
public class ConstSubstitutionTransformer<T> implements ASMTransformer {

    private final ConstSubstitutionRule<T> rule;

    public ConstSubstitutionTransformer(ConstSubstitutionRule<T> rule) {
        this.rule = rule;
    }

    @Override
    public String getName() {
        return Optional.ofNullable(rule).map(ConstSubstitutionRule::getClassName).orElse("");
    }

    @Override
    public ClassVisitor getClassVisitor(ClassWriter classWriter) {
        return new ConstSubstitutionVisitor<>(classWriter, rule);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
