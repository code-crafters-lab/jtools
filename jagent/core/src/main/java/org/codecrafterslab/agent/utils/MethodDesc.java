package org.codecrafterslab.agent.utils;

import org.objectweb.asm.Type;

import java.lang.reflect.Modifier;

/**
 * 方法描述生成器，将 ASM 字节码格式的方法信息转换为可读的 Java 方法签名
 *
 * <p>支持解析访问修饰符、返回类型、参数列表、异常声明和泛型签名，
 * 输出格式如：public static void pay(String param0, int param1) throws IOException
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 */
public class MethodDesc {

    /**
     * 复用 StringBuilder 实例，避免重复创建对象开销
     */
    private final static StringBuilder sb = new StringBuilder();

    /**
     * 生成方法的完整可读描述文本
     *
     * @param access     方法访问标志
     * @param name       方法名
     * @param descriptor 方法描述符
     * @param signature  泛型签名，可为 null
     * @param exceptions 抛出的异常类名数组，可为 null
     * @return 标准化方法描述文本
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
     * 解析特殊方法名
     *
     * @param name ASM 方法名
     * @return 规范化方法名
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
     * 将 ASM Type 转换为可读的 Java 类型名
     *
     * @param type ASM Type 对象
     * @return 类型名，如 java.lang.String、int、List
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

    /**
     * 生成方法描述的简化重载，适用于无泛型和异常的场景
     *
     * @param access     方法访问标志
     * @param name       方法名
     * @param descriptor 方法描述符
     * @return 方法描述文本
     */
    public static String generate(int access, String name, String descriptor) {
        return generate(access, name, descriptor, null, null);
    }
}
