package org.codecrafterslab.agent.core.asm.visitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time 2018/01/10 13:07
 */
public abstract class BaseClassVisitor extends ClassVisitor implements Opcodes {
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    public BaseClassVisitor(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    public BaseClassVisitor(ClassVisitor classVisitor) {
        this(ASM5, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        /* 先得到原始的方法,并可同时修改方法权限修饰符描述等 */
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);

        /* 修改方法, 删除则返回 null */
        if (this.isMethodModifySupport(access, name, desc, signature, exceptions)) {
            /* 在原始方法上修改 */
            if (log.isDebugEnabled()) {
                log.debug("Find method : {} {} {}", access, name, desc);
            }
            MethodVisitor methodVisitor = getModifyMethod(mv, access, name, desc);
            /* 修改不为空，则返回修改 */
            if (methodVisitor != null) {
                return methodVisitor;
            }
        }

        /* 否则返回原方法 */
        return mv;
    }

    /**
     * 指定方法是否支持修改
     *
     * @param access     修饰符
     * @param name       名称
     * @param desc       描述
     * @param signature  签名
     * @param exceptions 异常
     * @return boolean
     */
    protected abstract boolean isMethodModifySupport(int access, String name, String desc, String signature, String[] exceptions);

    /**
     * 修改方法
     *
     * @param original MethodVisitor
     * @param access   修饰符
     * @param name     名称
     * @param desc     权限
     * @return MethodVisitor
     */
    protected MethodVisitor getModifyMethod(MethodVisitor original, int access, String name, String desc) {
        return null;
    }

}
