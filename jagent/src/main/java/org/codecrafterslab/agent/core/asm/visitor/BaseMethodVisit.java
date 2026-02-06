package org.codecrafterslab.agent.core.asm.visitor;


import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time 2018/01/10 13:15
 */
public class BaseMethodVisit extends AdviceAdapter implements Opcodes {
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    public BaseMethodVisit(MethodVisitor methodVisitor, int access, String name, String desc) {
        super(ASM9, methodVisitor, access, name, desc);
    }

}
