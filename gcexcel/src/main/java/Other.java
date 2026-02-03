import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import com.grapecity.documents.excel.internals.aU.b;

import com.grapecity.documents.excel.internals.aV.h;
import com.grapecity.documents.excel.internals.aU.a;
import com.grapecity.documents.excel.internals.aZ.m;
import com.grapecity.documents.excel.internals.bJ.bO;

@Slf4j
public class Other {
    public static void logClassFiled(Class<?> clazz) {
        // 1. 获取目标类的 Class 对象（方式1：类名.class，最推荐，编译期可检查）
        try {
            // 1. 获取目标类的 Class 对象（方式1：类名.class，最推荐，编译期可检查）
            Field[] allDeclaredFields = clazz.getDeclaredFields();

            // 3. 遍历所有属性，筛选出静态属性并进行操作
            for (Field field : allDeclaredFields) {
                // 判断当前 Field 是否为静态属性（核心：Modifier.isStatic()）
                if (Modifier.isStatic(field.getModifiers())) {
                    System.out.println("====================================");
                    // 3.1 打印静态属性的基本信息（名称、类型、修饰符）
                    System.out.println("静态属性名称：" + field.getName());
                    System.out.println("静态属性类型：" + field.getType().getSimpleName());
                    System.out.println("静态属性修饰符：" + Modifier.toString(field.getModifiers()));

                    // 3.2 访问静态属性的值（关键：私有属性需先设置可访问，突破访问权限检查）
                    // setAccessible(true)：忽略 Java 访问权限修饰符的检查（即使是 private 也能访问）
                    field.setAccessible(true);
                    // 静态属性无需实例，get() 方法传入 null 即可获取值
                    Object fieldValue = field.get(null);
                    System.out.println("静态属性当前值：" + fieldValue);
                }
            }

        } catch (IllegalAccessException e) {
            log.error("访问静态属性失败", e);
        }
    }

    public static void licInfo() {
        String gcexcelJavaDeployLicenseV9 = System.getenv("GCEXCEL_JAVA_DEPLOY_LICENSE_V9");
        log.info("GCEXCEL_JAVA_DEPLOY_LICENSE_V9: {}", gcexcelJavaDeployLicenseV9);
        // NoLicense InvalidLicense ProductActivated ProductExpired TrialActivated TrialExpired
        log.debug("{} {} {} {} {} {}", a.a, a.b, a.c, a.d, a.e, a.f);
        // DevV2 DeployV2 ProductV1
        log.debug("{} {} {}", com.grapecity.documents.excel.internals.aU.f.a,
                com.grapecity.documents.excel.internals.aU.f.b, com.grapecity.documents.excel.internals.aU.f.c);
        // Evaluation -1
        // Unlicensed 0
        // Licensed 1
        log.debug("{} {} {} ", h.a, h.b, h.c);
        log.debug("{} {} {} {}", m.a, m.b, m.c, m.d);
        // Success ParseFailure MachineNameMismatch OtherProduct
        log.debug("{} {} {} {}", com.grapecity.documents.excel.internals.aX.e.a,
                com.grapecity.documents.excel.internals.aX.e.b, com.grapecity.documents.excel.internals.aX.e.c, com.grapecity.documents.excel.internals.aX.e.d);

        // InvariantCulture InvariantCultureIgnoreCase Ordinal OrdinalIgnoreCase
        log.debug("{} {} {} {}", bO.a, bO.b, bO.c, bO.d);

        b li = com.grapecity.documents.excel.internals.aU.e.a();
        log.info("授权状态：{}", li.a());
        log.info("授权状态：{}", li.b());
        log.info("授权状态：{}", li.c());
    }
}
