package aW;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import aV.f;
import bJ.bR;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public final class c {
    public static final Locale a = new Locale("", "", "");
    public static final DateFormat b;
    public static final DateFormat c;

    public c() {
    }

    public static String a(f var0) {
        if (var0 == null) {
            return "";
        } else {
            StringBuilder var1 = new StringBuilder();
            var1.append("{");
            if (var0.k != null) {
                var1.append(String.format(Locale.ROOT, "\"%s\":", "Anl"));
                var1.append("{");
                var1.append(String.format(Locale.ROOT, "\"%s\":%s", "dsr", var0.k.toString().toLowerCase(a)));
                if (!bR.a(var0.l)) {
                    var1.append(",");
                    var1.append(String.format(Locale.ROOT, "\"%s\":\"%s\"", "ver", var0.l.toLowerCase(a)));
                }

                var1.append("},");
            }

            var1.append(String.format(Locale.ROOT, "\"%s\":\"%s\",", "Id", var0.a));
            if (var0.b) {
                var1.append(String.format(Locale.ROOT, "\"%s\":%s,", "Evl", Boolean.toString(var0.b).toLowerCase(a)));
            }

            if (!bR.a(var0.c)) {
                var1.append(String.format(Locale.ROOT, "\"%s\":\"%s\",", "OId", var0.c));
            }

            if (!bR.a(var0.d)) {
                var1.append(String.format(Locale.ROOT, "\"%s\":\"%s\",", "CNa", var0.d));
            }

            if (!bR.a(var0.e)) {
                var1.append(String.format(Locale.ROOT, "\"%s\":\"%s\",", "CId", var0.e));
            }

            if (!bR.a(var0.f)) {
                var1.append(String.format(Locale.ROOT, "\"%s\":\"%s\",", "Dms", var0.f));
            }

            if (!bR.a(var0.g)) {
                var1.append(String.format(Locale.ROOT, "\"%s\":\"%s\",", "Ips", var0.g));
            }

            if (var0.h != null) {
                var1.append(String.format(Locale.ROOT, "\"%s\":\"%s\",", "Exp", b.format(var0.h)));
            }

            var1.append(String.format(Locale.ROOT, "\"%s\":\"%s\",", "Crt", c.format(var0.i)));
            var1.append(String.format(Locale.ROOT, "\"%s\":[%s]", "Prd", bR.a(",", a(var0.j))));
            var1.append("}");
            return var1.toString();
        }
    }

    public static aV.e a(String var0) {
        if (bR.a(var0)) {
            return null;
        } else {
            aV.e var1 = new aV.e();

            try {
                Gson var2 = new Gson();
                JsonObject var3 = var2.fromJson(var0, JsonObject.class);
                if (var3.has("S")) {
                    var1.e = var3.get("S").getAsString();
                }

                if (var3.has("D")) {
                    JsonObject var4 = var3.getAsJsonObject("D");
                    f var5 = new f();
                    if (var4.has("Id")) {
                        var5.a = var4.get("Id").getAsString();
                    }

                    if (var4.has("Evl")) {
                        var5.b = var4.get("Evl").getAsBoolean();
                    }

                    if (var4.has("OId")) {
                        var5.c = var4.get("OId").getAsString();
                    }

                    if (var4.has("CNa")) {
                        var5.d = var4.get("CNa").getAsString();
                    }

                    if (var4.has("CId")) {
                        var5.e = var4.get("CId").getAsString();
                    }

                    if (var4.has("Dms")) {
                        var5.f = var4.get("Dms").getAsString();
                    }

                    if (var4.has("Ips")) {
                        var5.g = var4.get("Ips").getAsString();
                    }

                    if (var4.has("Exp")) {
                        String var6 = var4.get("Exp").getAsString();
                        if (var6 != null) {
                            var5.h = b.parse(var6);
                        }
                    }

                    if (var4.has("Crt")) {
                        String var12 = var4.get("Crt").getAsString();
                        if (var12 != null) {
                            var5.i = c.parse(var12);
                        }
                    }

                    JsonObject var13 = var4.getAsJsonObject("Anl");
                    if (var13 != null) {
                        if (var13.has("dsr")) {
                            var5.k = var13.get("dsr").getAsBoolean();
                        }

                        if (var13.has("ver")) {
                            var5.l = var13.get("ver").getAsString();
                        }
                    }

                    JsonArray var7 = var4.getAsJsonArray("Prd");
                    if (var7 != null && var7.size() > 0) {
                        aV.i[] var8 = new aV.i[var7.size()];

                        for(int var9 = 0; var9 < var7.size(); ++var9) {
                            aV.i var10 = new aV.i();
                            var10.a = var7.get(var9).getAsJsonObject().get("N").getAsString();
                            var10.b = var7.get(var9).getAsJsonObject().get("C").getAsString();
                            var8[var9] = var10;
                        }

                        var5.j = var8;
                    }

                    var1.f = var5;
                } else {
                    var1.f = null;
                }
            } catch (Exception var11) {
                var1 = null;
            }

            return var1;
        }
    }

    private static String[] a(aV.i[] var0) {
        String[] var1 = new String[var0.length];

        for(int var2 = 0; var2 < var0.length; ++var2) {
            var1[var2] = a(var0[var2]);
        }

        return var1;
    }

    private static String a(aV.i var0) {
        return String.format(Locale.ROOT, "{\"N\":\"%s\",\"C\":\"%s\"}", var0.a, var0.b);
    }

    static {
        b = new SimpleDateFormat("yyyyMMdd", a);
        c = new SimpleDateFormat("yyyyMMdd hhmmss", a);
    }
}
