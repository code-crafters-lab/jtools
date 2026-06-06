import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.grapecity.documents.excel.Workbook;
import com.grapecity.documents.excel.internals.F.bR;
import com.grapecity.documents.excel.internals.aV.k;
import com.grapecity.documents.excel.internals.aU.e;
import com.grapecity.documents.excel.template.DataSource.l;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.lang.reflect.Field;

@Slf4j
public class GCDemo {

    public static void main(String[] args) throws Exception {

        String licenseV9 = System.getenv("GCEXCEL_JAVA_DEPLOY_LICENSE_V9");
        log.info("licenseV9: {}", licenseV9);
        // ----- DEBUG: inspect V9 license state -----
//        String v9Key = System.getenv("GCEXCEL_JAVA_DEPLOY_LICENSE_V9");
//        log.info("V9 env var: {}", v9Key != null ? v9Key.substring(0, Math.min(50, v9Key.length())) : "null");
//
//        // Reflect on aV.k state BEFORE
//        try {
//            java.lang.reflect.Field aField = e.class.getDeclaredField("a");
//            aField.setAccessible(true);
//            k aVk = (k) aField.get(null);
//            log.info("BEFORE - aV.k state: e()={}, f()={}, g()={}, b='{}'",
//                    aVk.e(), aVk.f(), aVk.g(), aVk.c());
//        } catch (Throwable t) {
//            log.warn("BEFORE read failed: {}", t.getMessage());
//        }
//
//        // Trigger V9 key load via aU.e.a() factory reflection
//        try {
//            Class<?> aUeClass = Class.forName("com.grapecity.documents.excel.internals.aU.e");
//            java.lang.reflect.Method setLic = aUeClass.getDeclaredMethod("a", String.class);
//            setLic.setAccessible(true);
//            setLic.invoke(null, v9Key);
//            log.info("aU.e.a() called with V9 key");
//        } catch (Throwable t) {
//            log.warn("aU.e.a() failed: {}", t.getMessage(), t);
//        }
//
//        // Reflect on aV.k state AFTER
//        try {
//            java.lang.reflect.Field aField = e.class.getDeclaredField("a");
//            aField.setAccessible(true);
//            k aVk = (k) aField.get(null);
//            log.info("AFTER - aV.k state: e()={}, f()={}, g()={}",
//                    aVk.e(), aVk.f(), aVk.g());
//            java.lang.reflect.Field dField = aVk.getClass().getDeclaredField("d");
//            dField.setAccessible(true);
//            Object d = dField.get(aVk);
//            log.info("AFTER - aV.k.d = {}", d);
//            if (d != null) {
//                java.lang.reflect.Method cMethod = d.getClass().getMethod("c");
//                Object state = cMethod.invoke(d);
//                log.info("AFTER - aV.k.d.c() = {}", state);
//                // Get signature
//                java.lang.reflect.Method aMethod = d.getClass().getMethod("a");
//                log.info("AFTER - aV.k.d.a() (sig) = {}", aMethod.invoke(d));
//            }
//        } catch (Throwable t) {
//            log.warn("AFTER read failed: {}", t.getMessage(), t);
//        }

        Workbook workbook = new Workbook();
        workbook.open("demo_tpl.xlsx");
        Test test = new Test();
        test.setA("a");
        test.setB(new BigDecimal("1235.5678"));
        test.setC(Test.H.C);

        // 注册自定义序列化器,序列为基本数据类型
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Test.H.class, new TypeAdapter<Test.H>() {
                    @Override
                    public void write(JsonWriter writer, Test.H h) throws IOException {
                        writer.value(h.name().toLowerCase() + "_001");
                    }

                    @Override
                    public Test.H read(JsonReader reader) throws IOException {
                        String s = reader.nextString();
                        return Test.H.valueOf(s.toUpperCase());
                    }
                })
                .create();

        JsonDS data = new JsonDS(test, gson);

        String createdAt = String.format("数据生成时间：%s", Instant.now().atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        workbook.addDataSource("created_at", createdAt);
        workbook.addDataSource("a", test.a);
        // b,c 无法正常填充
        workbook.addDataSource("b", test.b);
        workbook.addDataSource("c", test.c);

        // 使用自定义数据源后，可以使用了
        workbook.addDataSource("ds", data);
        workbook.processTemplate();
        workbook.save("demo.xlsx");


    }

    @Data
    static class Test {
        private String a;
        private BigDecimal b;
        private H c;

        enum H {
            A, B, C
        }
    }

    static class JsonDS implements l {
        private final JsonElement jsonElement;

        public JsonDS(Object data, Gson gson) {
            jsonElement = gson.toJsonTree(data);
        }

        @Override
        public Object a(boolean b) {
            // 这个参数的意义应该是：是否按照基本数据类型将数据递归展开
            if (b) {
                return bR.a(jsonElement);
            }
            return jsonElement;
        }
    }

}
