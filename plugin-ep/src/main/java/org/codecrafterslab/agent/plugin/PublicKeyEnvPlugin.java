package org.codecrafterslab.agent.plugin;

import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;
import org.codecrafterslab.agent.api.ITransformer;
import org.codecrafterslab.agent.api.Plugin;
import org.codecrafterslab.agent.core.plugin.BasePlugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.instrument.IllegalClassFormatException;
import java.util.Collections;
import java.util.List;

import static org.objectweb.asm.tree.AbstractInsnNode.FIELD_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.VAR_INSN;

@Slf4j
@AutoService(Plugin.class)
public class PublicKeyPlugin extends BasePlugin {

    @Override
    public List<ITransformer> getTransformers() {
        return Collections.singletonList(new PublicKeyTransformer());
    }

    static class PublicKeyTransformer implements ITransformer, Opcodes {

        @Override
        public String getName() {
            return "java.security.spec.RSAPublicKeySpec";
        }

        @Override
        public int getOrder() {
            return 0;
        }

        @Override
        public byte[] getCode(byte[] classBytes, int order) throws IllegalClassFormatException {
            if (classBytes == null || classBytes.length == 0) return null;
            // 1. 读取字节码
            ClassReader reader = new ClassReader(classBytes);

            // 3. 创建自定义 ClassVisitor，传入 ClassWriter
            ClassNode classNode = new ClassNode(Opcodes.ASM9);
            // 4. 解析类字节码，触发访问器回调（开始处理）
            // SKIP_DEBUG：跳过调试信息，提升效率；SKIP_FRAMES：跳过原始栈帧，由ClassWriter自动计算
            reader.accept(classNode, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

            for (MethodNode method : classNode.methods) {
                if (method.access == ACC_PUBLIC && "<init>".equals(method.name)
                        && ("(Ljava/math/BigInteger;Ljava/math/BigInteger;Ljava/security/spec/AlgorithmParameterSpec;)V").equals(method.desc)) {
                    insertTestFilterLogic(method);
                }
            }

            // 2. COMPUTE_FRAMES：自动计算栈帧和局部变量表，简化开发（无需手动处理栈操作）
            ClassWriter classWriter = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            classNode.accept(classWriter);

            // 5. 返回修改后的字节码
            return classWriter.toByteArray();
        }
    }

    /**
     * 插入你的testFilter逻辑（修正后）
     */
    private static void insertTestFilterLogic(MethodNode method) {
        InsnList list = new InsnList();
        // 混淆版局部变量索引：
        // 0: this  1: var1(modulus)  2: var2(publicExponent)  3: var3(params)
        // 4: var4(testFilter返回的数组)

        // ========== 你的核心逻辑（修正栈操作+指令顺序） ==========
        // 1. 调用 ArgsFilter.testFilter(var1, var2)
        list.add(new VarInsnNode(Opcodes.ALOAD, 1)); // 加载 var1
        list.add(new VarInsnNode(Opcodes.ALOAD, 2)); // 加载 var2
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                "org/codecrafterslab/agent/plugin/ArgsFilter",
                "match",
                "(Ljava/math/BigInteger;Ljava/math/BigInteger;)[Ljava/math/BigInteger;",
                false));
        list.add(new VarInsnNode(Opcodes.ASTORE, 4)); // 存到 var4（local4）

        // 2. 判断 var4 是否为 null
        list.add(new VarInsnNode(Opcodes.ALOAD, 4)); // 先加载var4 → 栈顶：var4
        list.add(new InsnNode(Opcodes.ACONST_NULL)); // 加载null → 栈顶：null，次顶：var4
        LabelNode label0 = new LabelNode();
        list.add(new JumpInsnNode(Opcodes.IF_ACMPEQ, label0)); // var4 == null → 跳去 label0

        // 3. var4 非 null → 替换 var1 = var4[0]
        list.add(new VarInsnNode(Opcodes.ALOAD, 4)); // 加载 var4 数组
        list.add(new InsnNode(Opcodes.ICONST_0)); // 数组下标 0
        list.add(new InsnNode(Opcodes.AALOAD)); // 取 var4[0]
        list.add(new VarInsnNode(Opcodes.ASTORE, 1)); // 替换 var1（local1）

        // 4. 替换 var2 = var4[1]
        list.add(new VarInsnNode(Opcodes.ALOAD, 4)); // 加载 var4 数组
        list.add(new InsnNode(Opcodes.ICONST_1)); // 数组下标 1
        list.add(new InsnNode(Opcodes.AALOAD)); // 取 var4[1]
        list.add(new VarInsnNode(Opcodes.ASTORE, 2)); // 替换 var2（local2）

        // 5. 标签：var4为 null 时跳过替换
        list.add(label0);

        // 寻找代码插入节点
        for (AbstractInsnNode node : method.instructions) {
            // 匹配：this.modulus = var1 对应的指令（关键！）
            // ALOAD 1
            // PUTFIELD java/security/spec/RSAPublicKeySpec.modulus Ljava/math/BigInteger
            if (VAR_INSN == node.getType() && Opcodes.ALOAD == node.getOpcode()
                    && FIELD_INSN == node.getNext().getType() && Opcodes.PUTFIELD == node.getNext().getOpcode()) {
                VarInsnNode varInsnNode = (VarInsnNode) node;
                FieldInsnNode fieldInsnNode = (FieldInsnNode) node.getNext();
                if (varInsnNode.var == 1 && "modulus".equals(fieldInsnNode.name) && "Ljava/math/BigInteger;".equals(fieldInsnNode.desc)) {
                    method.instructions.insertBefore(node, list);
                    break; // 找到位置后立即退出，避免重复插入
                }
            }
        }
    }
}
