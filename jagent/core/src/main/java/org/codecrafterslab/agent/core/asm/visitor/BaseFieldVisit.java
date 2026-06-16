package org.codecrafterslab.agent.core.asm.visitor;


import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ASM 字段访问器基类，用于扩展字段级别的字节码转换
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time 2020/04/29 21:36
 */
public class BaseFieldVisit extends FieldVisitor implements Opcodes {

    /**
     * 日志实例
     */
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * 创建字段访问器
     *
     * @param fv 下一个字段访问器
     */
    public BaseFieldVisit(FieldVisitor fv) {
        super(ASM9, fv);
    }

}
