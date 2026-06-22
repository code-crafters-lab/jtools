package org.codecrafterslab.agent.plugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * 修改 PublicKeySpec 三参数构造方法的 ClassVisitor
 */
class RSAPublicKeyClassVisitor extends ClassVisitor implements Opcodes {

    public RSAPublicKeyClassVisitor(ClassVisitor classVisitor) {
        super(ASM9, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

        // 匹配目标构造方法：方法名 <init> + 三参数描述符
        if (access == ACC_PUBLIC && "<init>".equals(name)
                && ("(Ljava/math/BigInteger;Ljava/math/BigInteger;Ljava/security/spec/AlgorithmParameterSpec;)V").equals(descriptor)) {
            return new RSAPublicKeyConstructorMethodVisitor(mv);
        }
        return mv;
    }


}

