import com.grapecity.documents.excel.Workbook;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class GCDemo {

    public static void main(String[] args) {
        Workbook workbook = new Workbook();
        workbook.open("demo_tpl.xlsx");
//        workbook.addConvert(BigDecimal.class, new IConvert<BigDecimal>() {
//            @Override
//            public String convert(BigDecimal value) {
//                return value.toString();
//            }
//        });
        Test test = new Test();
        test.setA("a");
        test.setB(new BigDecimal("1235.5678"));
        test.setC(Test.H.C);
        workbook.addDataSource("a", test.a);
        workbook.addDataSource("b", test.b);
        workbook.addDataSource("c", test.c);
        workbook.processTemplate();
        workbook.save("demo.xlsx");

        // 创建计数器为1的CountDownLatch
        CountDownLatch countDownLatch = new CountDownLatch(1);

//        try {
//            // 阻塞当前线程，直到计数器归0（此处永不归0）
//            countDownLatch.await();
//        } catch (InterruptedException e) {
//            System.out.println("程序被中断，即将退出");
//        }
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

}
