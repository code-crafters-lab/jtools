package aV;

import com.grapecity.documents.excel.EventArgs;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class a {
    private static final ConcurrentHashMap<String, e> a = new ConcurrentHashMap<>();
    private static final Locale b = new Locale("", "", "");
    private final c c;
    private final bJ.ap<Character, aW.a> d;
    private final bJ.ap<Character, aW.d> e;
    private final bJ.c<Boolean, bJ.ao<b>> f;
    private final aW.b<EventArgs> g;
    private static final String h = "#A0";
    private boolean i = false; // 是否需要校验签名

    public a(c var1, bJ.ap<Character, aW.a> var2, bJ.ap<Character, aW.d> var3, bJ.c<Boolean, bJ.ao<b>> var4) {
        this.c = var1;
        this.d = var2;
        this.e = var3;
        this.f = var4;
        this.g = new d<>(this);
        this.c.d().addListener(this.g);
    }

    private boolean b() {
        return a.containsKey(this.c.a());
    }

    private e c() {
        e var1 = a.get(this.c.a());
        if (var1 != null && !bJ.bR.a(var1.e) && var1.f != null && var1.f.j != null) {
            for (int var2 = 0; var2 < var1.f.j.length; ++var2) {
                if (var1.f.j[var2].b.equals(this.c.a())) {
                    return var1;
                }
            }
        }

        return null;
    }

    private b a(e var1) {
        String var2 = var1.a(this.c.a());
        String var3 = var1.b;
        h var4;
        if (var1.f == null) {
            var4 = aV.h.b;
        } else {
            var4 = var1.f.b ? aV.h.a : aV.h.c;
        }

        boolean var5 = var1.b(); // 是否过期
        Integer var6 = var1.a();
        Boolean var7 = null;
        if (var1.f != null && var1.f.k != null) {
            var7 = var1.f.k;
        }

        String[] var8 = var1.c;
        if (var8 == null) {
            var8 = new String[0];
        }

        return new j(var2, var3, var4, var5, var6, var7, var8);
    }

    public void a() throws Exception {
        if (this.b()) {
            e var1 = this.c();
        } else {
            e var4 = this.a(this.c.c(), this.d.invoke('c').b());
            aW.d var2 = this.e.invoke('s');
            if (!this.i && !var2.a(String.format(Locale.ROOT, "%s%s%s", var4.b, var4.a, var4.f == null ? "" : aW.c.a(var4.f)), var4.e)) {
                throw new Exception("Verify signature failed, Invalid License");
            }

            bJ.ao<b> var3 = () -> this.a(var4);
//            this.f.invoke(this.i ? false : this.a(var4.f), var3);
            boolean a1 = this.a(var4.f);
            this.f.invoke(this.i ? false : a1, var3);
        }

    }

    private e a(String var1, String var2) throws Exception {
        if (bJ.bR.a(var1)) {
            throw new Exception(com.grapecity.documents.excel.internals.bi.a.eI());
        } else {
            int var3 = -1;
            String var4 = var1.toLowerCase(b);
            String var5 = var2.toLowerCase(b);
            String var6 = "#A0".toLowerCase(b);
            var3 = var4.indexOf(var5);
            if (var3 == -1) {
                var3 = var4.indexOf(var6);
                if (var3 == -1) {
                    throw new Exception(com.grapecity.documents.excel.internals.bi.a.eI());
                }

                this.i = true;
                var2 = "#A0";
            }

            try {
                String var7 = var1.substring(0, var3);
                String var8 = var1.substring(var3 + var2.length());
                aW.a var9 = new aW.a("c", "A1");
                String var10 = var9.b(var8);
                e var11 = aW.c.a(var10);
                var11.a = var2;
                var11.b = var7;
                return var11;
            } catch (Exception var12) {
                throw new Exception("Invalid keyData");
            }
        }
    }

    private boolean a(f var1) {
        if (var1 == null) {
            return false;
        } else if (var1.j != null && var1.j.length != 0) {
            boolean var2 = false;

            for (int var3 = 0; var3 < var1.j.length; ++var3) {
                if (var1.j[var3].b.equals(this.c.a())) {
                    var2 = true;
                    break;
                }
            }

            if (!var2) {
                return false;
            } else {
                if (var1.h != null) {
                    int var4 = (int) ((var1.h.getTime() - (new Date()).getTime()) / 86400000L);
                    if (var4 <= 0) {
                        return false;
                    }
                }

                if (var1.k != null && var1.k) {
                    return false;
                } else {
                    boolean var5 = !bJ.bR.a(var1.f) || !bJ.bR.a(var1.g) && !this.a(this.c.b(), var1.f, var1.g, var1.k != null);
                    log.info("验证通过：{}", !var5);
                    return !var5;
                }
            }
        } else {
            return false;
        }
    }

    private boolean a(String var1, String var2, String var3, boolean var4) {
        if (bJ.bR.a(var1)) {
            return true;
        } else {
            if (!bJ.bR.a(var2)) {
                String[] var6 = var2.split(",");

                for (int var7 = 0; var7 < var6.length; ++var7) {
                    String var5 = var6[var7];
                    if (!bJ.bR.a(var5)) {
                        boolean var9 = false;
                        if (var5.startsWith("*.")) {
                            var9 = true;
                        }

                        String var8 = var9 ? var5.substring(2) : var5;
                        if (bJ.Q.a.a(var1, var8)) {
                            return true;
                        }

                        if (var9 && var1.toLowerCase(b).endsWith("." + var8.toLowerCase(b))) {
                            return true;
                        }

                        if (var4 && var1.toLowerCase(b).endsWith("." + var8.toLowerCase(b))) {
                            return true;
                        }
                    }
                }
            }

            if (!bJ.bR.a(var3)) {
                try {
                    InetAddress[] var11 = InetAddress.getAllByName(var1);
                    String[] var12 = new String[var11.length];

                    for (int var13 = 0; var13 < var11.length; ++var13) {
                        var12[var13] = var11[var13].getHostAddress();
                    }

                    String[] var14 = var3.split(",");

                    for (int var15 = 0; var15 < var14.length; ++var15) {
                        if (!bJ.bR.a(var14[var15]) && Arrays.asList(var12).contains(var14[var15])) {
                            return true;
                        }
                    }
                } catch (Exception var10) {
                }
            }

            return false;
        }
    }
}
