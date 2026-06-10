package aX;

import java.io.File;
import com.grapecity.documents.excel.internals.aY.d;
import com.grapecity.documents.excel.internals.bJ.am;
import com.grapecity.documents.excel.internals.bJ.bD;
import com.grapecity.documents.excel.internals.bJ.bR;
import com.grapecity.documents.excel.internals.bJ.bn;
import com.grapecity.documents.excel.internals.bJ.bq;

public final class b {
    private static c a;
    private static String b;
    private static String c;

    public b() {
    }

    public static synchronized void a(String var0) {
        b = var0;
        a = null;
    }

    public static synchronized void b(String var0) {
        c = var0;
        a = null;
    }

    public static synchronized c a() {
        if (a == null) {
            a = c();
        }

        return a;
    }

    private static String b() {
        boolean var0 = bD.a(bn.d);
        String var1 = var0 ? d.a() : d.b();
        return bq.b(var1, "GrapeCity");
    }

    private static c c() {
        String var0 = null;
        String var1 = b;
        String var2 = c;
        boolean var3;
        if (var1 != null) {
            var0 = var1;
            var3 = true;
        } else {
            String var4 = System.getenv("GCEXCEL_JAVA_DEPLOY_LICENSE_V9");
            if (!bR.a(var4)) {
                var0 = var4;
                var3 = true;
            } else {
                String var5;
                if (var2 != null) {
                    var5 = var2;
                    var3 = true;
                } else {
                    String var6 = b();
                    var5 = c(var6);
                    var3 = false;
                }

                if ((new File(var5)).isFile()) {
                    var0 = am.a(var5);
                    if (!bR.a(var0)) {
                        var0 = var0.trim();
                    }
                }
            }
        }

        return a(var0, var3);
    }

    private static c a(String var0, boolean var1) {
        if (var0 != null && !bR.a(var0)) {
            String[] var2 = var0.split("\n");
            if (var2.length < 2) {
                return new c(var0, var1);
            } else {
                c var3 = null;

                for(String var7 : var2) {
                    if (!bR.b(var7)) {
                        var3 = new c(var7.trim(), var1);
                        if (var3.n()) {
                            break;
                        }
                    }
                }

                return var3;
            }
        } else {
            return new c(var0, var1);
        }
    }

    private static String c(String var0) {
        String var1 = bq.a(var0, f.b().toString(), ".license");
        if (!(new File(var1)).isFile()) {
            var1 = bq.a(var0, f.c().toString(), ".license");
        }

        return var1;
    }
}
