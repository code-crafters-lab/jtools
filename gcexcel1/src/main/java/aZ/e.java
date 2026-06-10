package aZ;

import java.util.concurrent.atomic.AtomicInteger;

import aU.a;
import com.grapecity.documents.excel.internals.bJ.ab;
import com.grapecity.documents.excel.internals.bJ.ak;
import com.grapecity.documents.excel.internals.bJ.v;

public final class e {
    private static final f a = new f();
    private static final g b;
    private static final g c;
    private static final g d;
    private static final g e;
    private static final g f;
    private static final g g;
    private static final AtomicInteger h;

    public e() {
    }

    public static void a() {
        aU.b var0 = aU.e.a();
        a(var0).a(var0);
    }

    public static void b() {
        aU.b var0 = aU.e.a();
        a(var0).b(var0);
    }

    public static String a(m var0, ab var1) {
        aU.b var2 = aU.e.a();
        return a(var2).a(var2, var0, var1);
    }

    public static void c() {
        aU.b var0 = aU.e.a();
        a(var0).c(var0);
    }

    public static void d() {
        if (h.get() == 0) {
            c();
        }

        h.set((h.get() + 1) % 10000);
    }

    private static g a(aU.b var0) {
        if (var0 == null) {
            return c;
        } else {
            a at = var0.a();
            switch (at) {
                case a:
                    return c;
                case b:
                    return d;
                case c: // 产品已激活
                case e: // 试用激活
                    switch (var0.b()) {
                        case a:
                            return e;
                        case b:
                            return f;
                        case c:
                            return g;
                        default:
                            return d;
                    }
                case d: // 产品已过期
                case f: // 试用过期
                    return b;
                default:
                    return d;
            }
        }
    }

    public static void a(String var0) {
        aU.e.a(var0);
    }

    public static void b(String var0) {
        aU.e.b(var0);
    }

    public static void e() {
        a.b = v.n().e((double)-1.0F);
        a.a = v.n().e((double)-1.0F);
    }

    public static void a(Exception var0) {
        if (j.a(var0)) {
            throw ak.a(var0);
        }
    }

    public static void f() {
        aU.b var0 = aU.e.a();
        assert var0 != null;
        if (var0.a() == aU.a.a && a.d <= 100) {
            ++a.d;
        }

    }

    public static void g() {
        aU.b var0 = aU.e.a();
        assert var0 != null;
        if (var0.a() == aU.a.a && a.c <= 100) {
            ++a.c;
        }

    }

    static {
        b = new c(a);
        c = new k(a);
        d = new d(a);
        e = new aZ.b(a);
        f = new aZ.a(a);
        g = new l(a);
        h = new AtomicInteger();
    }
}
