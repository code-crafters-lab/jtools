import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.grapecity.documents.excel.Workbook;
import com.grapecity.documents.excel.internals.F.bT;
import com.grapecity.documents.excel.template.DataSource.JsonDataSource;
import com.grapecity.documents.excel.template.DataSource.l;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class GCDemo {

    public static void main(String[] args) throws Exception {
        // 新版授权使用方式
        String lic = System.getenv("GCEXCEL_JAVA_DEPLOY_LICENSE_V9");
        Workbook.SetLicenseKey(lic);

        Workbook workbook = new Workbook();

        FileInputStream inputStream = new FileInputStream("/Users/wuyujie/Project/jqsoft/teamwork/teamwork-procure/src/main/resources/template/business/商务采购批量入库.xlsx");
        workbook.open(inputStream);
//        Test test = new Test();
//        test.setA("a");
//        test.setB(new BigDecimal("1235.5678"));
//        test.setC(Test.H.C);
//        new JsonDataSource("");
//
//        // 注册自定义序列化器,序列为基本数据类型
//        Gson gson = new GsonBuilder()
//                .registerTypeAdapter(Test.H.class, new TypeAdapter<Test.H>() {
//                    @Override
//                    public void write(JsonWriter writer, Test.H h) throws IOException {
//                        writer.value(h.name().toLowerCase() + "_001");
//                    }
//
//                    @Override
//                    public Test.H read(JsonReader reader) throws IOException {
//                        String s = reader.nextString();
//                        return Test.H.valueOf(s.toUpperCase());
//                    }
//                })
//                .create();
//
//        JsonDS data = new JsonDS(test, gson);
//
//        String createdAt = String.format("数据生成时间：%s", Instant.now().atZone(ZoneId.systemDefault())
//                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//        workbook.addDataSource("created_at", createdAt);
//        workbook.addDataSource("a", test.a);
//        // b,c 无法正常填充
//        workbook.addDataSource("b", test.b);
//        workbook.addDataSource("c", test.c);
        String createdAt = String.format("模板生成时间：%s", Instant.now().atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        workbook.addDataSource("created_at", createdAt);

        List<D> data = new ArrayList<>();

        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        Reader reader = new FileReader("/Users/wuyujie/Project/opensource/jtools/gcexcel/src/main/resources/2060202252712804352.json");
        JsonElement element = JsonParser.parseReader(reader);

        if (element.isJsonArray()) {
            for (JsonElement jsonElement : element.getAsJsonArray()) {
                if (jsonElement.isJsonObject()) {
                    jsonElement.getAsJsonObject().addProperty("yesOrNot", "否");
                }
            }
        }

        for (int id : Arrays.asList(1, 2, 3, 4, 5, 6)) {
            D build = D.builder()
                    .procureId(String.valueOf(id))
                    .procureCode(String.format("%04d", id))
                    .product(element)
                    .build();
            data.add(build);
        }

        String json = gson.toJson(data);
        JsonDataSource ds = new JsonDataSource(json);

        // 使用自定义数据源后，可以使用了
        workbook.addDataSource("ds", ds);
        workbook.processTemplate();
        workbook.save("demo.xlsx");
    }

    @Builder
    static
    class D {
        String procureId;
        String procureCode;
        JsonElement product;
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

//    static class JsonDS implements l {
//        private final JsonElement jsonElement;
//
//        public JsonDS(Object data, Gson gson) {
//            jsonElement = gson.toJsonTree(data);
//        }
//
//        @Override
//        public Object a(boolean b) {
//            // 这个参数的意义应该是：是否按照基本数据类型将数据递归展开
//            if (b) {
//                return bT.a(jsonElement);
//            }
//            return jsonElement;
//        }
//    }

}
