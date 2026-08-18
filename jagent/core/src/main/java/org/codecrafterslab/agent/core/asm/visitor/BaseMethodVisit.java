package org.codecrafterslab.agent.core.asm.visitor;


import org.codecrafterslab.agent.logger.Logger;
import org.codecrafterslab.agent.logger.impl.LoggerFactory;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * ASM 方法访问器基类，基于 AdviceAdapter 简化方法增强开发
 *
 * <p>支持便捷的方法入口/出口字节码注入
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time 2018/01/10 13:15
 */
public class BaseMethodVisit extends AdviceAdapter implements Opcodes {

    /**
     * 日志实例
     */
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * 创建方法访问器
     *
     * @param methodVisitor 下一个方法访问器
     * @param access        方法访问修饰符
     * @param name          方法名称
     * @param desc          方法描述符
     */
    public BaseMethodVisit(MethodVisitor methodVisitor, int access, String name, String desc) {
        super(ASM9, methodVisitor, access, name, desc);
    }

}
