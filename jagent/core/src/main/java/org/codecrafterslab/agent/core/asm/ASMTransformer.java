package org.codecrafterslab.agent.core.asm;

import org.codecrafterslab.agent.api.ITransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.lang.instrument.IllegalClassFormatException;

/**
 * ASM 字节码转换器接口，封装 ASM 读写流程
 *
 * <p>实现此接口只需关注 ClassVisitor 的自定义逻辑，
 * 自动处理 ClassReader 和 ClassWriter 的初始化
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time 2020/07/07 09:02
 */
public interface ASMTransformer extends ITransformer, Opcodes {

    @Override
    default byte[] getCode(byte[] sourceBytes, int order) throws IllegalClassFormatException {
        if (sourceBytes == null || sourceBytes.length == 0) return null;
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

    /**
     * 获取自定义类访问器，实现具体字节码转换逻辑
     *
     * @param classWriter ASM ClassWriter
     * @return ClassVisitor 实现，null 表示不做任何转换
     */
    ClassVisitor getClassVisitor(ClassWriter classWriter);

}
