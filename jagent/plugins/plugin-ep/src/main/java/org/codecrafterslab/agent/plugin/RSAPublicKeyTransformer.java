package org.codecrafterslab.agent.plugin;

import org.codecrafterslab.agent.api.ITransformer;
import org.objectweb.asm.*;

class RSAPublicKeyTransformer implements ITransformer, Opcodes {
    @Override
    public String getClassName() {
        return "java.security.spec.RSAPublicKeySpec";
    }

    @Override
    public int getOrder() {
        return 0;
    }

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
