import lombok.extern.slf4j.Slf4j;
import org.codecrafterslab.agent.core.asm.ASMTransformer;
import org.objectweb.asm.*;

@Slf4j
public class ModulusTransformer implements ASMTransformer {

    @Override
    public String getCanonicalName() {
        return "com.grapecity.documents.excel.internals.aX.a";
    }

    @Override
    public ClassVisitor getClassVisitor(ClassWriter classWriter) {
        return new ModulusClassVisitor(classWriter);
    }

}
