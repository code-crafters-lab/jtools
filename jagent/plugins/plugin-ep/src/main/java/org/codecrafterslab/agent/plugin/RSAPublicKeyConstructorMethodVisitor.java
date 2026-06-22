package org.codecrafterslab.agent.plugin;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class RSAPublicKeyConstructorMethodVisitor extends MethodVisitor implements Opcodes {

    private boolean superInitInvoked = false;

    // 替换为你实际的 ArgsFilter 类全限定名（斜杠分隔包名）
    private static final String ARGS_FILTER_OWNER = "org/codecrafterslab/agent/plugin/ArgsFilter";
    // match 方法描述符：两个 BigInteger 参数，返回 BigInteger 数组
    private static final String MATCH_METHOD_DESC = "(Ljava/math/BigInteger;Ljava/math/BigInteger;)[Ljava/math/BigInteger;";

    public RSAPublicKeyConstructorMethodVisitor(MethodVisitor mv) {
        super(ASM9, mv);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        // 先执行原指令（保证 super.<init> 先被调用）
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);

        // 捕获第一个 invokespecial <init>，即父类 Object 构造方法调用
        if (opcode == INVOKESPECIAL && "<init>".equals(name) && !superInitInvoked) {
            // 插入自定义参数过滤逻辑
            insertFilterLogic();
            superInitInvoked = true;
        }
    }

    /**
     * 插入 ArgsFilter.match 调用 + 非空赋值逻辑
     */
    private void insertFilterLogic() {
        // 0: this  1: var1(modulus)  2: var2(publicExponent)  3: var3(params)
        // 4: var4(ArgsFilter.match返回的数组)

        // 1. 压入两个参数 var1、var2 作为 ArgsFilter.match 方法的入参
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);

        // 2. 调用静态方法 ArgsFilter.match
        mv.visitMethodInsn(INVOKESTATIC, ARGS_FILTER_OWNER, "match", MATCH_METHOD_DESC, false);

        // 3. 将返回数组存入局部变量 4（var4）
        int var4Index = 4;
        mv.visitVarInsn(ASTORE, var4Index);

        // 4. 非空判断：var4 == null 则跳转到结束标签
        Label endLabel = new Label();
        mv.visitVarInsn(ALOAD, var4Index);
        mv.visitJumpInsn(IFNULL, endLabel);

        // 5. var1 = var4[0]
        mv.visitVarInsn(ALOAD, var4Index); // 加载 var4 数组
        mv.visitInsn(ICONST_0); // 数组下标 0
        mv.visitInsn(AALOAD); // 取 var4[0]
        mv.visitVarInsn(ASTORE, 1); // 替换 var1（local1）

        // 6. var2 = var4[1]
        mv.visitVarInsn(ALOAD, var4Index); // 加载 var4 数组
        mv.visitInsn(ICONST_1); // 数组下标 1
        mv.visitInsn(AALOAD); // 取 var4[1]
        mv.visitVarInsn(ASTORE, 2); // 替换 var2（local2）

        // 7. 标记结束标签，后续继续执行原字段赋值逻辑
        mv.visitLabel(endLabel);
    }
}
