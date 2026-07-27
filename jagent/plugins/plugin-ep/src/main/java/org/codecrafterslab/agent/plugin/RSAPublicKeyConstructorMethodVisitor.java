package org.codecrafterslab.agent.plugin;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * RSAPublicKeySpec 三参数构造方法的字节码修改访问者
 *
 * <p>在构造方法执行完 {@code super.<init>} 调用后，注入 {@code ArgsFilter.match()} 调用，
 * 将构造参数（模数 modulus、指数 exponent）替换为预设值。
 *
 * <p>注入逻辑（伪代码）：
 * <pre>
 * // 原构造方法体执行到 super.&lt;init&gt; 调用之后
 * BigInteger[] result = ArgsFilter.match(modulus, publicExponent);
 * if (result != null) {
 *     modulus = result[0];       // 替换模数
 *     publicExponent = result[1]; // 替换指数
 * }
 * // 继续执行原字段赋值逻辑
 * </pre>
 *
 * <p>局部变量布局：
 * <ul>
 *   <li>0: this - 当前对象引用</li>
 *   <li>1: modulus - 公钥模数参数</li>
 *   <li>2: publicExponent - 公钥指数参数</li>
 *   <li>3: params - 算法参数规范（AlgorithmParameterSpec）</li>
 *   <li>4: ArgsFilter.match() 返回的 BigInteger 数组（临时变量）</li>
 * </ul>
 *
 * @author Wu Yujie
 * @since 1.0.0
 * @see ArgsFilter
 */
class RSAPublicKeyConstructorMethodVisitor extends MethodVisitor implements Opcodes {

    /**
     * 标记是否已拦截到 super.&lt;init&gt; 调用
     *
     * <p>构造方法中第一个 {@code INVOKESPECIAL <init>} 指令是调用父类构造方法，
     * 后续可能还有其他 &lt;init&gt; 调用（如内部类构造），此标志确保只处理一次
     */
    private boolean superInitInvoked = false;

    /**
     * ArgsFilter 类的内部路径名（斜杠分隔包名）
     *
     * <p>ASM 中类名使用 {@code /} 分隔包名，而非 {@code .}
     */
    private static final String ARGS_FILTER_OWNER = "org/codecrafterslab/agent/plugin/ArgsFilter";

    /**
     * ArgsFilter.match 方法的描述符
     *
     * <p>方法签名：{@code BigInteger[] match(BigInteger modulus, BigInteger exponent)}
     * <p>描述符含义：接收两个 BigInteger 参数，返回 BigInteger 数组
     */
    private static final String MATCH_METHOD_DESC = "(Ljava/math/BigInteger;Ljava/math/BigInteger;)[Ljava/math/BigInteger;";

    /**
     * 构造方法访问者
     *
     * @param mv 下游方法访问者（通常是 ClassWriter 的 MethodVisitor）
     */
    public RSAPublicKeyConstructorMethodVisitor(MethodVisitor mv) {
        super(ASM9, mv);
    }

    /**
     * 访问方法调用指令
     *
     * <p>拦截 {@code INVOKESPECIAL <init>} 调用（即 super.&lt;init&gt;），
     * 在其执行完成后插入自定义的参数过滤逻辑。
     *
     * <p>执行顺序：先调用 super.visitMethodInsn 执行原指令，
     * 再判断是否为第一次 &lt;init&gt; 调用，是则注入过滤逻辑
     *
     * @param opcode    操作码（INVOKESPECIAL）
     * @param owner     被调用方法所在类的内部名
     * @param name      方法名（&lt;init&gt;）
     * @param descriptor 方法描述符
     * @param isInterface 是否为接口方法调用
     */
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
     *
     * <p>注入的字节码逻辑：
     * <ol>
     *   <li>调用 {@code ArgsFilter.match(modulus, publicExponent)} 获取替换值</li>
     *   <li>判断返回值是否为 null</li>
     *   <li>若非 null，将返回数组的元素赋值给构造参数</li>
     *   <li>继续执行原构造方法的字段赋值逻辑</li>
     * </ol>
     *
     * <p>等效 Java 代码：
     * <pre>
     * BigInteger[] var4 = ArgsFilter.match(modulus, publicExponent);
     * if (var4 != null) {
     *     modulus = var4[0];
     *     publicExponent = var4[1];
     * }
     * </pre>
     */
    private void insertFilterLogic() {
        // 0: this  1: var1(modulus)  2: var2(publicExponent)  3: var3(params)
        // 4: var4(ArgsFilter.match返回的数组)

        // 1. 压入两个参数 var1、var2 作为 ArgsFilter.match 方法的入参
        visitVarInsn(ALOAD, 1);
        visitVarInsn(ALOAD, 2);

        // 2. 调用静态方法 ArgsFilter.match
        visitMethodInsn(INVOKESTATIC, ARGS_FILTER_OWNER, "match", MATCH_METHOD_DESC, false);

        // 3. 将返回数组存入局部变量 4（var4）
        int var4Index = 4;
        visitVarInsn(ASTORE, var4Index);

        // 4. 非空判断：var4 == null 则跳转到结束标签
        Label endLabel = new Label();
        visitVarInsn(ALOAD, var4Index);
        visitJumpInsn(IFNULL, endLabel);

        // 5. var1 = var4[0]
        visitVarInsn(ALOAD, var4Index); // 加载 var4 数组
        visitInsn(ICONST_0); // 数组下标 0
        visitInsn(AALOAD); // 取 var4[0]
        visitVarInsn(ASTORE, 1); // 替换 var1（local1）

        // 6. var2 = var4[1]
        visitVarInsn(ALOAD, var4Index); // 加载 var4 数组
        visitInsn(ICONST_1); // 数组下标 1
        visitInsn(AALOAD); // 取 var4[1]
        visitVarInsn(ASTORE, 2); // 替换 var2（local2）

        // 7. 标记结束标签，后续继续执行原字段赋值逻辑
        visitLabel(endLabel);
    }
}
