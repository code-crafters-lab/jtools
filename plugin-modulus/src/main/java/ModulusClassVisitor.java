import org.codecrafterslab.agent.core.asm.visitor.BaseClassVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class ModulusClassVisitor extends BaseClassVisitor {
    // 原始待替换的两个字符串（复制自目标类）
    public static final String ORIGINAL_STR1 = "tgoYDy+InG+V+F4gU9ssbjuTHXzHaXwFzyF" +
            "+SA85fe4AeN1N5jzxA2MzXT8VsArKZ9Ugz2rYPp9kOhpwiSq2QSfxE+axl8403O9JkcB9826e7Co3WjZYOMbfKWLrRJFTWatkEIRJvP2ocOEtYCLDOaET08OCAnSFAcO7fReSd00=";
    public static final String ORIGINAL_STR2 = "+US+f7+816LsJEa/Gd0daNSWKehDM7uF72OpCHfSECbjX9WlrrOxIq8kOgIVxkDfJeWmP6OwZw9Xn+VJa8Sxze7jKUopRH1awfK1p+RiQnOcmqUpi4GTUHK+6F9nSc/Y0T5H7pgDNSk8CkB/LwfaaCj0FmrEXns8fguG2l3VrCU=";

    // 自定义替换后的两个值A和B（请替换为你的合法Base64字符串）
    public static final String CUSTOM_STR_A = "AKr5BPhLiCKb3Rc0ZUVFMpSQUYX8CVac2akqS+C24k9eHgLmcTUcmWsuGrPbaAE6uxkEw7LPFJb4EtF0/YdWG7MsJZVOzC7fQ44+nt2L1SOwan5ZFLXlychnLi6VWMdB8d20Trcrq483JtTpWj+Af3rdnEecxijKK6PKDQVXKJPUKg31pHUQBeVUoLJaUpDJtJAyHXp8bY0OUMGp8GCnoF8UPOkCHLtbsx8VrMezfNFoWzoad3Dvg85ebUDJN0qsnmv7V9p+BgiOcuUzVdJ3Xnnv9PVsjm9bm5dWu//NcdrdErIMSpMqZWwaO3KppEokYni5BEvM69jfY5//XRCAYW0="; // 你的第一个自定义值
    public static final String CUSTOM_STR_B = "AJdDILyIft6d5cnu8khv06/SU4TYJhcfs090NvgchAySu0F3MvVA0ZxeIsSeun978CJfxY9DBlUlb3ReQzHlTiYD4SwoXtQ15wXEfR56sKNk1nrfmZ+nwej9G+n4ZIKGLwG9ikCiLqbgifYVWW0tm2Euxt81c9CDmMxCygSezpQOQNoP6zRWd+KlT6P5TQ7AVGqSgfr1qLQpQ6xjkw+s0UnHE5a4jWQim9E6k5HV/0P5X9yIv+vL3dh97hSTEpacyMzqD3o47Y9mvXjg+rE5XV7Zj0xQ2YVsgX0aqrsiiwSGUL2Mu42p7j0CBnVaSAuWNgh1v34OGqNMNdpdBfZGT8U="; // 你的第二个自定义值


    public ModulusClassVisitor(ClassVisitor classVisitor) {
        super(ASM9, classVisitor);
    }

    @Override
    protected boolean isMethodModifySupport(int access, String name, String desc, String signature, String[] exceptions) {
        // 匹配目标方法：private static PublicKey a()
        int i = ACC_PRIVATE | ACC_STATIC;
        return access == i && "a".equals(name) && "()Ljava/security/PublicKey;".equals(desc);
    }

    @Override
    protected MethodVisitor getModifyMethod(MethodVisitor original, int access, String name, String desc) {
        // 返回自定义MethodVisitor，处理该方法内的常量替换
        ModulusMethodVisitor modulusMethodVisitor = new ModulusMethodVisitor(original);
        modulusMethodVisitor.add(ORIGINAL_STR1, CUSTOM_STR_A);
        modulusMethodVisitor.add(ORIGINAL_STR2, CUSTOM_STR_B);
        return modulusMethodVisitor;
    }

}
