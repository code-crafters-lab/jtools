package org.codecrafterslab.agent.core.asm.visitor;

import org.codecrafterslab.agent.api.IMethodSupport;
import org.codecrafterslab.agent.utils.MethodDesc;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.Optional;

/**
 * ASM 类访问器基类，实现方法级别的字节码转换入口
 *
 * <p>通过 getMethodSupport() 定义方法匹配规则，
 * 匹配成功的方法交由 getModifyMethod() 进行转换
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time 2018/01/10 13:07
 */
public abstract class BaseClassVisitor extends ClassVisitor implements Opcodes {

    /**
     * 日志实例
     */
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * 创建类访问器
     *
     * @param api         ASM API 版本
     * @param classVisitor 下一个访问器
     */
    public BaseClassVisitor(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    /**
     * 创建类访问器，默认使用 ASM9
     *
     * @param classVisitor 下一个访问器
     */
    public BaseClassVisitor(ClassVisitor classVisitor) {
        this(ASM9, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        /* 先得到原始的方法,并可同时修改方法权限修饰符描述等 */
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

        /* 修改方法, 删除则返回 null */
        if (this.getMethodSupport().orElse(IMethodSupport.DEFAULT).accept(access, name, descriptor, signature, exceptions)) {
            /* 在原始方法上修改 */
            if (log.isDebugEnabled()) {
                String generate = MethodDesc.generate(access, name, descriptor, signature, exceptions);
                log.debug("Find method : {}", generate);
            }
            MethodVisitor methodVisitor = getModifyMethod(mv, access, name, descriptor);
            /* 修改不为空，则返回修改 */
            if (methodVisitor != null) {
                return methodVisitor;
            }
        }

        /* 否则返回原方法 */
        return mv;
    }

    /**
     * 获取方法匹配规则，用于判断方法是否需要转换
     *
     * @return 方法匹配规则
     */
    protected abstract Optional<IMethodSupport> getMethodSupport();

    /**
     * 修改匹配的方法，返回新的 MethodVisitor 实现自定义转换
     *
     * @param original 原始 MethodVisitor
     * @param access   方法访问修饰符
     * @param name     方法名称
     * @param desc     方法描述符
     * @return 自定义的 MethodVisitor，null 表示不做修改
     */
    protected MethodVisitor getModifyMethod(MethodVisitor original, int access, String name, String desc) {
        return null;
    }

}
