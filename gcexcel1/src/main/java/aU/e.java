package aU;

public class e {
    private static final aV.k a = new aV.k();

    public e() {
    }

    public static b a() {
        aV.k var0 = a;
        if (var0.e().equals(aV.h.b) && !var0.g()) {
            aX.c var1 = aX.b.a();
            return var1 != null ? b.a(var1) : null;
        } else {
            return b.a(a);
        }
    }

    public static void a(String var0) {
        boolean var1 = false;
        boolean var2 = c(var0);
        if (var2) {
            var1 = true;
        } else {
            aX.b.a(var0);
            var1 = b();
        }

        if (var1) {
            try {
                a.a(var0);
            } catch (Exception var4) {
                throw new IllegalStateException(var4);
            }
        }

    }

    private static boolean b() {
        boolean var0 = false;
        aX.c var1 = aX.b.a();
        if (var1 != null) {
            a var2 = var1.a();
            if (var2 == aU.a.a) {
                var0 = true;
            } else if (var2 == aU.a.b) {
                aX.e var3 = aX.d.a();
                if (var3 == aX.e.b) {
                    var0 = true;
                }
            }
        } else {
            var0 = true;
        }

        return var0;
    }

    private static boolean c(String var0) {
        return var0 != null && !var0.contains(";");
    }

    public static void b(String var0) {
        String var1 = com.grapecity.documents.excel.internals.bJ.am.a(var0);
        boolean var3 = c(var1);
        boolean var2;
        if (var3) {
            var2 = true;
        } else {
            aX.b.b(var0);
            var2 = b();
        }

        if (var2) {
            try {
                a.a(var1);
            } catch (Exception var5) {
                throw new IllegalStateException(var5);
            }
        }

    }
}
