import com.google.gson.*;
import com.grapecity.documents.excel.Workbook;
import com.grapecity.documents.excel.template.DataSource.JsonDataSource;
import com.jetbrains.F.F.a;
import com.jetbrains.ls.responses.EncodedAsset;
import com.jetbrains.ls.responses.License;
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
//        try {
//            EncodedAsset encodedAsset = new EncodedAsset("O2LMSDO6IH-eyJsaWNlbnNlSWQiOiJPMkxNU0RPNklIIiwibGljZW5zZWVOYW1lIjoiV3UgWXVqaWUiLCJsaWNlbnNlZVR5cGUiOiJQRVJTT05BTCIsImFzc2lnbmVlTmFtZSI6IiIsImFzc2lnbmVlRW1haWwiOiIiLCJsaWNlbnNlUmVzdHJpY3Rpb24iOiIiLCJjaGVja0NvbmN1cnJlbnRVc2UiOmZhbHNlLCJwcm9kdWN0cyI6W3siY29kZSI6IkdPIiwiZmFsbGJhY2tEYXRlIjoiMjAyNi0wMS0zMCIsInBhaWRVcFRvIjoiMjAyNy0wMS0yOSIsImV4dGVuZGVkIjpmYWxzZX0seyJjb2RlIjoiUlMwIiwiZmFsbGJhY2tEYXRlIjoiMjAyNi0wMS0zMCIsInBhaWRVcFRvIjoiMjAyNy0wMS0yOSIsImV4dGVuZGVkIjpmYWxzZX0seyJjb2RlIjoiRE0iLCJmYWxsYmFja0RhdGUiOiIyMDI2LTAxLTMwIiwicGFpZFVwVG8iOiIyMDI3LTAxLTI5IiwiZXh0ZW5kZWQiOmZhbHNlfSx7ImNvZGUiOiJDTCIsImZhbGxiYWNrRGF0ZSI6IjIwMjYtMDEtMzAiLCJwYWlkVXBUbyI6IjIwMjctMDEtMjkiLCJleHRlbmRlZCI6ZmFsc2V9LHsiY29kZSI6IkFDIiwicGFpZFVwVG8iOiIyMDIwLTA5LTEzIiwiZXh0ZW5kZWQiOmZhbHNlfSx7ImNvZGUiOiJSU1UiLCJmYWxsYmFja0RhdGUiOiIyMDI2LTAxLTMwIiwicGFpZFVwVG8iOiIyMDI3LTAxLTI5IiwiZXh0ZW5kZWQiOmZhbHNlfSx7ImNvZGUiOiJSU0MiLCJmYWxsYmFja0RhdGUiOiIyMDI2LTAxLTMwIiwicGFpZFVwVG8iOiIyMDI3LTAxLTI5IiwiZXh0ZW5kZWQiOnRydWV9LHsiY29kZSI6IlBDIiwiZmFsbGJhY2tEYXRlIjoiMjAyNi0wMS0zMCIsInBhaWRVcFRvIjoiMjAyNy0wMS0yOSIsImV4dGVuZGVkIjpmYWxzZX0seyJjb2RlIjoiRFMiLCJmYWxsYmFja0RhdGUiOiIyMDI3LTEyLTMxIiwicGFpZFVwVG8iOiIyMDI3LTAxLTI5IiwiZXh0ZW5kZWQiOmZhbHNlfSx7ImNvZGUiOiJSRCIsImZhbGxiYWNrRGF0ZSI6IjIwMjYtMDEtMzAiLCJwYWlkVXBUbyI6IjIwMjctMDEtMjkiLCJleHRlbmRlZCI6ZmFsc2V9LHsiY29kZSI6IlFBIiwiZmFsbGJhY2tEYXRlIjoiMjAyNi0xMi0zMSIsInBhaWRVcFRvIjoiMjAyNS0xMi0yMyIsImV4dGVuZGVkIjpmYWxzZX0seyJjb2RlIjoiUkMiLCJmYWxsYmFja0RhdGUiOiIyMDI2LTAxLTMwIiwicGFpZFVwVG8iOiIyMDI3LTAxLTI5IiwiZXh0ZW5kZWQiOmZhbHNlfSx7ImNvZGUiOiJSU0YiLCJmYWxsYmFja0RhdGUiOiIyMDI2LTAxLTMwIiwicGFpZFVwVG8iOiIyMDI3LTAxLTI5IiwiZXh0ZW5kZWQiOnRydWV9LHsiY29kZSI6IkRCUiIsImZhbGxiYWNrRGF0ZSI6IjIwMjYtMDEtMzAiLCJwYWlkVXBUbyI6IjIwMjctMDEtMjkiLCJleHRlbmRlZCI6ZmFsc2V9LHsiY29kZSI6IlJNIiwiZmFsbGJhY2tEYXRlIjoiMjAyNi0wMS0zMCIsInBhaWRVcFRvIjoiMjAyNy0wMS0yOSIsImV4dGVuZGVkIjpmYWxzZX0seyJjb2RlIjoiSUkiLCJmYWxsYmFja0RhdGUiOiIyMDI2LTAxLTMwIiwicGFpZFVwVG8iOiIyMDI3LTAxLTI5IiwiZXh0ZW5kZWQiOmZhbHNlfSx7ImNvZGUiOiJEUE4iLCJmYWxsYmFja0RhdGUiOiIyMDI2LTAxLTMwIiwicGFpZFVwVG8iOiIyMDI3LTAxLTI5IiwiZXh0ZW5kZWQiOmZhbHNlfSx7ImNvZGUiOiJEQiIsImZhbGxiYWNrRGF0ZSI6IjIwMjYtMDEtMzAiLCJwYWlkVXBUbyI6IjIwMjctMDEtMjkiLCJleHRlbmRlZCI6ZmFsc2V9LHsiY29kZSI6IkRDIiwiZmFsbGJhY2tEYXRlIjoiMjAyNi0wMS0zMCIsInBhaWRVcFRvIjoiMjAyNy0wMS0yOSIsImV4dGVuZGVkIjpmYWxzZX0seyJjb2RlIjoiUFMiLCJmYWxsYmFja0RhdGUiOiIyMDI2LTAxLTMwIiwicGFpZFVwVG8iOiIyMDI3LTAxLTI5IiwiZXh0ZW5kZWQiOmZhbHNlfSx7ImNvZGUiOiJSUiIsImZhbGxiYWNrRGF0ZSI6IjIwMjYtMDEtMzAiLCJwYWlkVXBUbyI6IjIwMjctMDEtMjkiLCJleHRlbmRlZCI6ZmFsc2V9LHsiY29kZSI6IlJTViIsImZhbGxiYWNrRGF0ZSI6IjIwMjYtMDEtMzAiLCJwYWlkVXBUbyI6IjIwMjctMDEtMjkiLCJleHRlbmRlZCI6dHJ1ZX0seyJjb2RlIjoiV1MiLCJmYWxsYmFja0RhdGUiOiIyMDI2LTAxLTMwIiwicGFpZFVwVG8iOiIyMDI3LTAxLTI5IiwiZXh0ZW5kZWQiOmZhbHNlfSx7ImNvZGUiOiJQU0kiLCJmYWxsYmFja0RhdGUiOiIyMDI3LTEyLTMxIiwicGFpZFVwVG8iOiIyMDI3LTAxLTI5IiwiZXh0ZW5kZWQiOnRydWV9LHsiY29kZSI6IlBDV01QIiwicGFpZFVwVG8iOiIyMDI3LTAxLTI5IiwiZXh0ZW5kZWQiOnRydWV9LHsiY29kZSI6IkFJTCIsInBhaWRVcFRvIjoiMjAyNy0wMS0yOSIsImV4dGVuZGVkIjpmYWxzZX0seyJjb2RlIjoiUlMiLCJmYWxsYmFja0RhdGUiOiIyMDI2LTAxLTMwIiwicGFpZFVwVG8iOiIyMDI3LTAxLTI5IiwiZXh0ZW5kZWQiOnRydWV9LHsiY29kZSI6IlZTQ1JTIiwiZmFsbGJhY2tEYXRlIjoiMjAyNi0wMS0zMCIsInBhaWRVcFRvIjoiMjAyNy0wMS0yOSIsImV4dGVuZGVkIjpmYWxzZX0seyJjb2RlIjoiUFJSIiwiZmFsbGJhY2tEYXRlIjoiMjAyNi0wMS0zMCIsInBhaWRVcFRvIjoiMjAyNy0wMS0yOSIsImV4dGVuZGVkIjp0cnVlfSx7ImNvZGUiOiJEUCIsImZhbGxiYWNrRGF0ZSI6IjIwMjYtMDEtMzAiLCJwYWlkVXBUbyI6IjIwMjctMDEtMjkiLCJleHRlbmRlZCI6dHJ1ZX0seyJjb2RlIjoiUERCIiwiZmFsbGJhY2tEYXRlIjoiMjAyNy0xMi0zMSIsInBhaWRVcFRvIjoiMjAyNy0wMS0yOSIsImV4dGVuZGVkIjp0cnVlfV0sIm1ldGFkYXRhIjoiMDQyMDI2MDUyOFBQ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");
//            License decode = encodedAsset.decode();
//        } catch (a e) {
//            throw new RuntimeException(e);
//        }
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
