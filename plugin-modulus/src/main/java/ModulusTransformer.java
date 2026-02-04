import org.codecrafterslab.agent.core.Transformer;
import org.objectweb.asm.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ModulusTransformer implements Transformer {
    // 原始待替换的两个字符串（复制自目标类）
    private static final String ORIGINAL_STR1 = "tgoYDy+InG+V+F4gU9ssbjuTHXzHaXwFzyF+SA85fe4AeN1N5jzxA2MzXT8VsArKZ9Ugz2rYPp9kOhpwiSq2QSfxE+axl8403O9JkcB9826e7Co3WjZYOMbfKWLrRJFTWatkEIRJvP2ocOEtYCLDOaET08OCAnSFAcO7fReSd00=";
    private static final String ORIGINAL_STR2 = "+US+f7+816LsJEa/Gd0daNSWKehDM7uF72OpCHfSECbjX9WlrrOxIq8kOgIVxkDfJeWmP6OwZw9Xn+VJa8Sxze7jKUopRH1awfK1p+RiQnOcmqUpi4GTUHK+6F9nSc/Y0T5H7pgDNSk8CkB/LwfaaCj0FmrEXns8fguG2l3VrCU=";

    // 自定义替换后的两个值A和B（请替换为你的合法Base64字符串）
    private static final String CUSTOM_STR_A = "AKr5BPhLiCKb3Rc0ZUVFMpSQUYX8CVac2akqS+C24k9eHgLmcTUcmWsuGrPbaAE6uxkEw7LPFJb4EtF0/YdWG7MsJZVOzC7fQ44+nt2L1SOwan5ZFLXlychnLi6VWMdB8d20Trcrq483JtTpWj+Af3rdnEecxijKK6PKDQVXKJPUKg31pHUQBeVUoLJaUpDJtJAyHXp8bY0OUMGp8GCnoF8UPOkCHLtbsx8VrMezfNFoWzoad3Dvg85ebUDJN0qsnmv7V9p+BgiOcuUzVdJ3Xnnv9PVsjm9bm5dWu//NcdrdErIMSpMqZWwaO3KppEokYni5BEvM69jfY5//XRCAYW0="; // 你的第一个自定义值
    private static final String CUSTOM_STR_B = "AJdDILyIft6d5cnu8khv06/SU4TYJhcfs090NvgchAySu0F3MvVA0ZxeIsSeun978CJfxY9DBlUlb3ReQzHlTiYD4SwoXtQ15wXEfR56sKNk1nrfmZ+nwej9G+n4ZIKGLwG9ikCiLqbgifYVWW0tm2Euxt81c9CDmMxCygSezpQOQNoP6zRWd+KlT6P5TQ7AVGqSgfr1qLQpQ6xjkw+s0UnHE5a4jWQim9E6k5HV/0P5X9yIv+vL3dh97hSTEpacyMzqD3o47Y9mvXjg+rE5XV7Zj0xQ2YVsgX0aqrsiiwSGUL2Mu42p7j0CBnVaSAuWNgh1v34OGqNMNdpdBfZGT8U="; // 你的第二个自定义值

    @Override
    public String getCanonicalName() {
        return "com.grapecity.documents.excel.internals.aX.a";
    }

    @Override
    public byte[] getCode(byte[] classBytes, int order) throws Exception {
        // 2. 读取字节码
        ClassReader reader = new ClassReader(classBytes);
        // COMPUTE_FRAMES：自动计算栈帧和局部变量表，简化开发（无需手动处理栈操作）
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        // 3. 创建自定义ClassVisitor，传入ClassWriter
        CustomClassVisitor classVisitor = new CustomClassVisitor(Opcodes.ASM9, classWriter);
        // 4. 解析类字节码，触发访问器回调（开始处理）
        // SKIP_DEBUG：跳过调试信息，提升效率；SKIP_FRAMES：跳过原始栈帧，由ClassWriter自动计算
        reader.accept(classVisitor, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

        // 5. 写出修改后的字节码到指定文件
        byte[] modifiedClassBytes = classWriter.toByteArray();
        String outputPath = "modified_a.class";
        Files.write(Paths.get(outputPath), modifiedClassBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

//        System.out.println("字节码修改完成，修改后的文件已保存至：" + outputPath);
        return modifiedClassBytes;
    }

    /**
     * 自定义ClassVisitor：匹配目标方法，创建自定义MethodVisitor
     */
    static class CustomClassVisitor extends ClassVisitor {
        public CustomClassVisitor(int api, ClassVisitor classVisitor) {
            super(api, classVisitor);
        }

        /**
         * 访问类的方法时触发（核心：匹配目标方法a()）
         *
         * @param access     方法访问标志（如ACC_PRIVATE、ACC_STATIC）
         * @param name       方法名
         * @param descriptor 方法描述符（()Ljava/security/PublicKey; 对应无参返回PublicKey）
         * @param signature  方法签名（null表示无泛型）
         * @param exceptions 方法抛出的异常（null表示无异常）
         * @return 自定义MethodVisitor（处理方法内的字节码）
         */
        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

            // 匹配目标方法：private static PublicKey a()
            // 条件1：方法名是"a"；条件2：访问标志包含ACC_STATIC（静态方法）；条件3：方法描述符匹配无参返回PublicKey
            if ("a".equals(name) && (access & Opcodes.ACC_STATIC) != 0
                    && "()Ljava/security/PublicKey;".equals(descriptor)) {
//                System.out.println("匹配到目标方法：a()，开始处理字节码...");
                // 返回自定义MethodVisitor，处理该方法内的常量替换
                return new CustomMethodVisitor(Opcodes.ASM9, mv);
            }

            return mv;
        }

        /**
         * 自定义MethodVisitor：拦截ldc指令，替换目标字符串
         */
        static class CustomMethodVisitor extends MethodVisitor {
            public CustomMethodVisitor(int api, MethodVisitor methodVisitor) {
                super(api, methodVisitor);
            }

            /**
             * 访问常量加载指令（ldc）时触发（核心：替换字符串常量）
             *
             * @param value 常量值（这里对应我们要替换的字符串）
             */
            @Override
            public void visitLdcInsn(Object value) {
                // 判断是否是目标原始字符串，若是则替换为自定义值
                if (ORIGINAL_STR1.equals(value)) {
//                    System.out.println("匹配到原始字符串1，替换为自定义值A...");
                    super.visitLdcInsn(CUSTOM_STR_A);
                } else if (ORIGINAL_STR2.equals(value)) {
//                    System.out.println("匹配到原始字符串2，替换为自定义值B...");
                    super.visitLdcInsn(CUSTOM_STR_B);
                } else {
                    // 非目标字符串，直接保留原始常量
                    super.visitLdcInsn(value);
                }
            }
        }
    }
}
