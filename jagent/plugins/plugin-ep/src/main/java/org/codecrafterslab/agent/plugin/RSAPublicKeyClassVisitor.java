package org.codecrafterslab.agent.plugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * RSAPublicKeySpec 类的访问者，用于定位并拦截目标构造方法
 *
 * <p>继承 {@link ClassVisitor}，在类访问过程中筛选出需要修改的方法。
 * 当访问到三参数构造方法时，返回自定义的 {@link RSAPublicKeyConstructorMethodVisitor}
 * 进行字节码注入。
 *
 * <p>目标方法签名：
 * {@code public RSAPublicKeySpec(BigInteger modulus, BigInteger publicExponent, AlgorithmParameterSpec params)}
 *
 * <p>方法描述符：{@code (Ljava/math/BigInteger;Ljava/math/BigInteger;Ljava/security/spec/AlgorithmParameterSpec;)V}
 *
 * @author Wu Yujie
 * @since 1.0.0
 * @see RSAPublicKeyConstructorMethodVisitor
 */
class RSAPublicKeyClassVisitor extends ClassVisitor implements Opcodes {

    /**
     * 构造类访问者
     *
     * @param classVisitor 下游访问者（通常是 ClassWriter），用于写出修改后的字节码
     */
    public RSAPublicKeyClassVisitor(ClassVisitor classVisitor) {
        super(ASM9, classVisitor);
    }

    /**
     * 访问类中的方法
     *
     * <p>筛选条件：
     * <ul>
     *   <li>访问修饰符为 {@code public}</li>
     *   <li>方法名为 {@code <init>}（构造方法）</li>
     *   <li>方法描述符匹配三参数版本：两个 BigInteger + 一个 AlgorithmParameterSpec</li>
     * </ul>
     *
     * <p>匹配成功后，使用 {@link RSAPublicKeyConstructorMethodVisitor} 替换默认的 MethodVisitor，
     * 在构造方法体中注入参数过滤逻辑
     *
     * @param access    方法访问修饰符
     * @param name      方法名
     * @param descriptor 方法描述符（参数类型和返回类型的编码）
     * @param signature 泛型签名（构造方法通常为 null）
     * @param exceptions 抛出的异常类型
     * @return 匹配目标方法时返回自定义 MethodVisitor，否则返回原始 MethodVisitor
     */
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
