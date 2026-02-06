import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.grapecity.documents.excel.Workbook;
import com.grapecity.documents.excel.internals.F.bR;
import com.grapecity.documents.excel.template.DataSource.l;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigDecimal;

@Slf4j
public class GCDemo {

    public static void main(String[] args) throws InterruptedException {

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
