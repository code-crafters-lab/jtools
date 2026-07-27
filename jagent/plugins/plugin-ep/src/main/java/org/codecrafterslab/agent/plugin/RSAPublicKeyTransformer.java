package org.codecrafterslab.agent.plugin;

import org.codecrafterslab.agent.api.ITransformer;
import org.objectweb.asm.*;

/**
 * RSAPublicKeySpec 类的字节码转换器
 *
 * <p>负责拦截 {@code java.security.spec.RSAPublicKeySpec} 类的加载过程，
 * 通过 ASM 框架修改其字节码，在三参数构造方法中注入参数过滤逻辑。
 *
 * <p>转换流程：
 * <ol>
 *   <li>接收原始类字节码</li>
 *   <li>使用 {@link ClassReader} 解析字节码结构</li>
 *   <li>通过 {@link RSAPublicKeyClassVisitor} 定位目标构造方法</li>
 *   <li>使用 {@link RSAPublicKeyConstructorMethodVisitor} 修改构造方法体</li>
 *   <li>返回修改后的字节码</li>
 * </ol>
 *
 * <p>目标类：{@code java.security.spec.RSAPublicKeySpec}
 * <p>目标方法：三参数构造方法 {@code RSAPublicKeySpec(BigInteger, BigInteger, AlgorithmParameterSpec)}
 *
 * @author Wu Yujie
 * @since 1.0.0
 * @see RSAPublicKeyClassVisitor
 * @see RSAPublicKeyConstructorMethodVisitor
 */
class RSAPublicKeyTransformer implements ITransformer, Opcodes {

    /**
     * 返回需要转换的目标类的全限定名
     *
     * @return 目标类名 {@code java.security.spec.RSAPublicKeySpec}
     */
    @Override
    public String getClassName() {
        return "java.security.spec.RSAPublicKeySpec";
    }

    /**
     * 返回转换器的执行优先级
     *
     * <p>优先级越小越先执行，0 表示最高优先级
     *
     * @return 优先级值，固定为 0
     */
    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * 执行字节码转换
     *
     * <p>使用 ASM 的 Visitor 模式修改类字节码：
     * <ul>
     *   <li>{@link ClassReader#SKIP_DEBUG}：跳过调试信息，减小输出体积</li>
     *   <li>{@link ClassReader#SKIP_FRAMES}：跳过原始栈帧，由 ClassWriter 自动计算</li>
     *   <li>{@link ClassWriter#COMPUTE_FRAMES}：自动计算栈帧和局部变量表</li>
     * </ul>
     *
     * @param sourceBytes 原始类字节码
     * @param order       转换器执行顺序
     * @return 修改后的字节码，若输入为空则返回 null
     */
    @Override
    public byte[] getCode(byte[] sourceBytes, int order) {
        if (sourceBytes == null || sourceBytes.length == 0) return null;
        // 1. 读取字节码
        ClassReader reader = new ClassReader(sourceBytes);
        // 2. COMPUTE_FRAMES：自动计算栈帧和局部变量表，简化开发（无需手动处理栈操作）
        ClassWriter classWriter = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
        // 3. 创建自定义 ClassVisitor，传入 ClassWriter
        ClassVisitor classVisitor = new RSAPublicKeyClassVisitor(classWriter);
        // 4. 解析类字节码，触发访问器回调（开始处理）
        // SKIP_DEBUG：跳过调试信息，提升效率；SKIP_FRAMES：跳过原始栈帧，由ClassWriter自动计算
        reader.accept(classVisitor, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        // 5. 返回修改后的字节码
        return classWriter.toByteArray();
    }

}
