package org.codecrafterslab.agent.utils;

import org.objectweb.asm.Type;

import java.lang.reflect.Modifier;

public class MethodDesc {
   private final static StringBuilder sb = new StringBuilder();
    /**
     * 核心方法：生成方法描述文本
     *
     * @param access     方法访问标志（如 Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC）
     * @param name       方法名（如 "pay"、"<init>"）
     * @param descriptor 方法描述符（如 "(Ljava/lang/String;I)V"）
     * @param signature  泛型签名（可为 null）
     * @param exceptions 方法抛出的异常类名数组（可为 null）
     * @return 标准化方法描述文本（如 "public static void pay(String orderId, int amount) throws IOException"）
     */
    public static String generate(int access, String name, String descriptor, String signature, String[] exceptions) {
        sb.setLength(0);

        // 1. 解析访问修饰符（public/static/final 等）
        String modifiers = Modifier.toString(access & Modifier.methodModifiers());
        if (!modifiers.isEmpty()) {
            sb.append(modifiers).append(" ");
        }

        // 2. 处理特殊方法名（构造方法/静态初始化块）
        String methodName = resolveMethodName(name);
        // 构造方法无返回值，普通方法解析返回类型
        if (!"<init>".equals(methodName)) {
            // 解析返回类型
            String returnType = resolveType(Type.getReturnType(descriptor));
            sb.append(returnType).append(" ");
        }

        // 3. 拼接方法名 + 参数列表
        sb.append(methodName).append("(");
        // 解析参数类型
        Type[] parameterTypes = Type.getArgumentTypes(descriptor);
        for (int i = 0; i < parameterTypes.length; i++) {
            String paramType = resolveType(parameterTypes[i]);
            // 简单参数名（可自定义，如 param0/param1，或结合泛型签名优化）
            String paramName = "param" + i;
            sb.append(paramType).append(" ").append(paramName);
            if (i < parameterTypes.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");

        // 4. 解析抛出的异常
        if (exceptions != null && exceptions.length > 0) {
            sb.append(" throws ");
            for (int i = 0; i < exceptions.length; i++) {
                // 将类名（如 java/io/IOException）转换为 java.io.IOException
                String exceptionName = exceptions[i].replace("/", ".");
                sb.append(exceptionName);
                if (i < exceptions.length - 1) {
                    sb.append(", ");
                }
            }
        }

        // 5. 可选：追加泛型签名（若有）
        if (signature != null && !signature.isEmpty()) {
            sb.append(" [泛型签名: ").append(signature).append("]");
        }

        return sb.toString();
    }

    /**
     * 解析特殊方法名：<init> → 构造方法（类名），<clinit> → 静态初始化块
     */
    private static String resolveMethodName(String name) {
        if ("<init>".equals(name)) {
            return "<init>"; // 构造方法，也可替换为目标类名（需额外传入类名参数）
        } else if ("<clinit>".equals(name)) {
            return "<clinit>"; // 静态初始化块
        } else {
            return name; // 普通方法名
        }
    }

    /**
     * 将 ASM Type 转换为易读的 Java 类型名
     *
     * @param type ASM Type 对象（如 Type.getType(String.class)）
     * @return 类型名（如 java.lang.String、int、List）
     */
    private static String resolveType(Type type) {
        switch (type.getSort()) {
            case Type.VOID:
                return "void";
            case Type.BOOLEAN:
                return "boolean";
            case Type.CHAR:
                return "char";
            case Type.BYTE:
                return "byte";
            case Type.SHORT:
                return "short";
            case Type.INT:
                return "int";
            case Type.LONG:
                return "long";
            case Type.FLOAT:
                return "float";
            case Type.DOUBLE:
                return "double";
            case Type.ARRAY:
                // 数组类型（如 [Ljava/lang/String; → java.lang.String[]）
                return resolveType(type.getElementType()) + "[]";
            case Type.OBJECT:
                // 引用类型（如 java/lang/String → java.lang.String）
                return type.getClassName();
            default:
                return type.getClassName();
        }
    }

    // 重载方法：无泛型/异常时简化调用
    public static String generate(int access, String name, String descriptor) {
        return generate(access, name, descriptor, null, null);
    }
}
