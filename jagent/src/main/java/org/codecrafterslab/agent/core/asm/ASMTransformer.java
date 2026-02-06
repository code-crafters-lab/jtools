package org.codecrafterslab.agent.core.asm;

import org.codecrafterslab.agent.core.Transformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public interface ASMTransformer extends Transformer {

    @Override
    default byte[] getCode(byte[] sourceBytes, int order) throws Exception {
        if (sourceBytes != null && sourceBytes.length > 0) {
            // 1. 读取字节码
            ClassReader reader = new ClassReader(sourceBytes);
            // 2. COMPUTE_FRAMES：自动计算栈帧和局部变量表，简化开发（无需手动处理栈操作）
            ClassWriter classWriter = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
            // 3. 创建自定义 ClassVisitor，传入 ClassWriter
            ClassVisitor classVisitor = this.getClassVisitor(classWriter);
            // 4. 解析类字节码，触发访问器回调（开始处理）
            // SKIP_DEBUG：跳过调试信息，提升效率；SKIP_FRAMES：跳过原始栈帧，由ClassWriter自动计算
            reader.accept(classVisitor == null ? classWriter : classVisitor, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            // 5. 返回修改后的字节码
            return classWriter.toByteArray();
        }
        return Transformer.super.getCode(sourceBytes, order);
    }

    /**
     * 获取类访问器
     *
     * @param classWriter ClassWriter
     * @return ClassVisitor
     */
    ClassVisitor getClassVisitor(ClassWriter classWriter);

}
