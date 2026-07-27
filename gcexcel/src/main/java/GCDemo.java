import com.google.gson.*;
import com.grapecity.documents.excel.Workbook;
import com.grapecity.documents.excel.template.DataSource.JsonDataSource;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * GcExcel 模板填充功能演示
 *
 * <p>演示如何使用 GcExcel 的模板填充能力：
 * <ul>
 *   <li>读取 Excel 模板文件</li>
 *   <li>绑定 JSON 数据源</li>
 *   <li>添加简单数据源（如时间戳）</li>
 *   <li>执行模板填充并导出结果</li>
 * </ul>
 *
 * @author GcExcel Demo
 * @since 1.0.0
 */
@Slf4j
public class GCDemo {

    /**
     * 程序入口，加载模板和数据并执行填充
     *
     * @param args 命令行参数（未使用）
     * @throws Exception 读取资源或处理模板时可能抛出的异常
     */
    public static void main(String[] args) throws Exception {
        // 从环境变量读取 V9 版本授权密钥，设置到工作簿中（非空时生效）
        String lic = System.getenv("GCEXCEL_JAVA_DEPLOY_LICENSE_V9");
        if (lic != null && !lic.isEmpty()) {
            Workbook.SetLicenseKey(lic);
        }
        // 创建空白工作簿，后续用于加载模板
        Workbook workbook = new Workbook();

        try (
                // 从 classpath 加载 Excel 模板文件
                InputStream tpl = GCDemo.class.getClassLoader().getResourceAsStream("demo_tpl.xlsx");
                // 从 classpath 加载 JSON 数据文件
                InputStream data = GCDemo.class.getClassLoader().getResourceAsStream("demo_data.json");
        ) {
            // 将模板加载到工作簿中
            workbook.open(Objects.requireNonNull(tpl));
            // 将 JSON 输入流转换为字符串，用于绑定数据源
            InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(data));
            JsonElement jsonElement = JsonParser.parseReader(inputStreamReader);
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            String json = gson.toJson(jsonElement);
            // 执行模板填充
            GCDemo.renderData(workbook, json);
        }
    }

    /**
     * 绑定数据源并执行模板填充
     *
     * <p>向模板添加两个数据源：
     * <ul>
     *   <li>created_at - 模板生成时间</li>
     *   <li>ds - JSON 业务数据</li>
     * </ul>
     *
     * @param workbook 已加载模板的工作簿对象
     * @param json     JSON 格式的业务数据
     */
    private static void renderData(Workbook workbook, String json) {
        // 生成当前时间作为简单数据源，格式化为 yyyy-MM-dd HH:mm:ss
        String createdAt = String.format("模板生成时间：%s", Instant.now().atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        // 绑定简单字符串数据源，模板中通过 {{created_at}} 引用
        workbook.addDataSource("created_at", createdAt);
        workbook.addDataSource("num_prefix","库存");
        // 绑定 JSON 数据源，模板中通过 {{product.xxx}} 引用嵌套字段
        workbook.addDataSource("product", new JsonDataSource(json));
        // 执行模板填充，将数据源替换到模板占位符中
        workbook.processTemplate();
        // 导出填充后的 Excel 文件
        workbook.save("demo.xlsx");
    }

}
