package aX;

import java.util.Locale;
import java.util.UUID;
import com.grapecity.documents.excel.internals.bJ.ce;
import com.grapecity.documents.excel.internals.bJ.v;
import com.grapecity.documents.excel.internals.bJ.bV;
import com.grapecity.documents.excel.internals.bJ.bR;
import com.grapecity.documents.excel.internals.bJ.bO;
import com.grapecity.documents.excel.internals.bJ.bD;
import com.grapecity.documents.excel.internals.bJ.bn;

public final class c {
    private boolean a;
    private String b;

    public final aU.a a() {
        bJ.bw<aU.a> var2 = new bJ.bw<>();
        bJ.bw<String> var3 = new bJ.bw<>();
        this.a(var2, var3);
        return var2.a;
    }

    public final boolean b() {
        return this.a() == aU.a.c;
    }

    public final String c() {
        bJ.bw<aU.a> var2 = new bJ.bw<>();
        bJ.bw<String> var3 = new bJ.bw<>();
        this.a(var2, var3);
        return var3.a;
    }

    public final boolean d() {
        return this.o().f;
    }

    public final String e() {
        return this.o() == null ? "" : this.o().h;
    }

    public final String f() {
        return this.o() == null ? "" : this.o().i;
    }

    public final boolean g() {
        return this.o() == null ? false : this.o().a;
    }

    public final String h() {
        return this.o() == null ? "" : this.o().j;
    }

    public final String i() {
        return this.o() == null ? "" : this.o().c;
    }

    public final String j() {
        a var1 = this.o();
        return var1 == null ? "" : this.o().d;
    }

    public c(String var1, boolean var2) {
        this.a(var1);
        this.a = var2;
    }

    public final String k() {
        return this.b;
    }

    private void a(String var1) {
        this.b = var1;
    }

    private a o() {
        a var1 = b(this.k());
        if (var1 == null) {
            d.a(e.b);
            return null;
        } else {
            boolean var2 = this.a(var1);
            if (!var2) {
                d.a(e.d);
                com.grapecity.documents.excel.internals.aY.b.a(3, 1, "Product or major version mismatch.");
                return null;
            } else {
                return var1;
            }
        }
    }

    public final f l() {
        a var1 = this.o();
        if (var1 == null) {
            return null;
        } else {
            boolean var2 = f.b().equals(var1.b);
            boolean var3 = f.c().equals(var1.b);
            if (var2) {
                return f.b;
            } else {
                return var3 ? f.c : null;
            }
        }
    }

    private static a b(String var0) {
        if (bJ.bR.a(var0)) {
            com.grapecity.documents.excel.internals.aY.b.a(2, 1);
            return null;
        } else {
            String[] var1 = var0.split(";", -1);
            if (var1.length != 2) {
                com.grapecity.documents.excel.internals.aY.b.a(2, 2);
                return null;
            } else if (!aX.a.a(var1[0], var1[1])) {
                com.grapecity.documents.excel.internals.aY.b.a(2, 3);
                return null;
            } else {
                try {
                    return c(var1[0]);
                } catch (Exception var3) {
                    com.grapecity.documents.excel.internals.aY.b.a(2, 4);
                    return null;
                }
            }
        }
    }

    private static a c(String var0) {
        if (bJ.bR.a(var0)) {
            return null;
        } else {
            String[] var1 = var0.split(",", -1);
            if (var1.length != 11) {
                return null;
            } else {
                a var2 = new a();
                var2.b = UUID.fromString(d(var1[0]));
                var2.c = d(var1[1]);
                var2.d = d(var1[2]);
                var2.a = Boolean.parseBoolean(d(var1[3]));
                var2.e = Integer.parseInt(d(var1[4]));
                var2.f = Boolean.parseBoolean(d(var1[5]));
                var2.g = Integer.parseInt(d(var1[6]));
                var2.h = d(var1[8]);
                var2.i = d(var1[9]);
                var2.j = d(var1[10]);
                return var2;
            }
        }
    }

    private static String d(String var0) {
        return bJ.bR.a(var0) ? "" : ce.c().a(aX.a.a(var0));
    }

    private static int a(v var0, int var1) {
        if (var1 == 0) {
            return Integer.MAX_VALUE;
        } else if (var0.a(v.B().a((double)1.0F)) > 0) {
            return -1;
        } else if (var1 > 0) {
            v var5 = a(var1);
            return v.c(var5, v.B()).b();
        } else {
            v var2 = v.a(var0, bV.a((double)(-var1)));
            v var3 = v.B();
            bV var4 = v.c(var2, var3);
            return var4.b();
        }
    }

    private static v a(int var0) {
        return v.a(new v(2000, 1, 1), bV.a((double)var0));
    }

    public final int m() {
        a var1 = this.o();
        if (var1 == null) {
            return -1;
        } else {
            v var2 = a(var1.e);
            return a(var2, var1.g);
        }
    }

    public final boolean n() {
        boolean var1 = this.g() || bR.d(this.e(), "Unlimited", bO.d);
        if (var1) {
            return true;
        } else {
            String var2 = com.grapecity.documents.excel.internals.aY.a.a();
            if (bR.a(var2)) {
                return false;
            } else {
                String var3 = this.j();
                if (bR.a(var3)) {
                    return false;
                } else {
                    if (bD.a(bn.d) && var2.length() > 15 && var3.length() == 15) {
                        var2 = var2.substring(0, 15);
                    }

                    return var2.equalsIgnoreCase(var3);
                }
            }
        }
    }

    public final void a(bJ.bw<aU.a> var1, bJ.bw<String> var2) {
        d.b();
        if (bJ.bR.a(this.k())) {
            d.a(e.b);
            var1.a = aU.a.a;
            var2.a = "No License";
        } else {
            var1.a = aU.a.b;
            var2.a = "Invalid License";
            a var3 = this.o();
            if (var3 != null) {
                if (!this.n()) {
                    d.a(e.c);
                    com.grapecity.documents.excel.internals.aY.b.a(1, 1, "Machine Environment mismatch.");
                } else {
                    int var4 = this.m();
                    boolean var5 = var4 < 0;
                    String var6 = var4 == Integer.MAX_VALUE ? "" : String.format(Locale.ROOT, " (%1$s days left)", var4);
                    if (var3.f) {
                        var1.a = var5 ? aU.a.f : aU.a.e;
                        var2.a = var5 ? "Trial License, Expired" : "Trial License, Activated" + var6;
                    } else {
                        var1.a = var5 ? aU.a.d : aU.a.c;
                        var2.a = var5 ? "Product License, Expired" : "Product License, Activated" + var6;
                    }

                }
            }
        }
    }

    private boolean a(a var1) {
        boolean var2 = f.b().equals(var1.b);
        boolean var3 = f.c().equals(var1.b);
        if (this.a) {
            if (!var2) {
                return var3 && var1.a;
            }
        } else if (!var2 && !var3) {
            return false;
        }

        return true;
    }

    private static class a {
        private boolean a;
        private UUID b;
        private String c;
        private String d;
        private int e;
        private boolean f; // 是否试用
        private int g;
        private String h;
        private String i;
        private String j;

        private a() {
        }
    }
}
