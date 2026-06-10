package aZ;

import com.grapecity.documents.excel.bq;
import com.grapecity.documents.excel.internals.bj.a;


public final class h {
    public h() {
    }

    public static String a(String var0) {
        bq var1 = bq.g();
        String var2 = a.a(var1.a());
        String var3 = a.a(var1.b());
        String var4 = a.a(var1.c());
        String var5 = a.a(var1.d());
        String var6 = a.a(var1.f());
        String var7 = a.a(var1.e());
        return var0.replace("{Company}", var2).replace("{Mail}", var3).replace("{PurchaseLink}", var4).replace("{PhoneNumber}", var5).replace("{ProductFullName}", var6).replace("{ProductShortName}", var7);
    }
}
