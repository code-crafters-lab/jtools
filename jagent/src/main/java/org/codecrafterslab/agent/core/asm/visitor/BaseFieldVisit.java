package org.codecrafterslab.agent.core.asm.visitor;


import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time 2020/04/29 21:36
 */
public class BaseFieldVisit extends FieldVisitor implements Opcodes {
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    public BaseFieldVisit(FieldVisitor fv) {
        super(ASM9, fv);
    }

}
