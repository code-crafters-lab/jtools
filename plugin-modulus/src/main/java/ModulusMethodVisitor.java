import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ModulusMethodVisitor extends MethodVisitor {

    private Map<String, String> replacer;

    public ModulusMethodVisitor(int api, MethodVisitor methodVisitor, Map<String, String> replacer) {
        super(api, methodVisitor);
        this.replacer = replacer;
    }

    public ModulusMethodVisitor(MethodVisitor methodVisitor, Map<String, String> replacer) {
        this(Opcodes.ASM9, methodVisitor, replacer);
    }

    public ModulusMethodVisitor(MethodVisitor methodVisitor) {
        this(Opcodes.ASM9, methodVisitor, null);
    }

    public void add(String key, String value) {
        if (replacer == null) {
            replacer = new HashMap<>();
        }
        replacer.put(key, value);
    }


    /**
     * 访问常量加载指令（ldc）时触发（核心：替换字符串常量）
     *
     * @param value 常量值（这里对应我们要替换的字符串）
     */
    @Override
    public void visitLdcInsn(Object value) {
        if (value instanceof String) {
            if (replacer != null && replacer.containsKey(value)) {
                String val = replacer.get(value);
                if (val != null && !val.isEmpty()) {
                    if (log.isDebugEnabled()) {
                        log.debug("src value: {}", value);
                        log.debug("dst value: {}", val);
                    }
                    super.visitLdcInsn(val);
                    return;
                }
            }
        }
        super.visitLdcInsn(value);
    }
}
