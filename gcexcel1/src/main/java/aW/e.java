package aW;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public final class e {
    private static final int a = 48;

    public e() {
    }

    public static String a(String var0) throws UnsupportedEncodingException {
        return f(e(a(c(var0).replace("==", "&").replace("=", "#"), true)));
    }

    public static String b(String var0) throws UnsupportedEncodingException {
        return d(a(e(g(var0)), false).replace("#", "=").replace("&", "=="));
    }

    public static String c(String var0) {
        return bJ.h.b(var0.getBytes(StandardCharsets.UTF_8));
    }

    public static String d(String var0) {
        return new String(bJ.h.a(var0.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    public static String a(String var0, boolean var1) {
        int var2 = var0.length() / 2 + (var0.length() % 2 != 0 && !var1 ? 1 : 0);
        return var0.substring(var2) + var0.substring(0, var2);
    }

    public static String e(String var0) {
        return (new StringBuilder(var0)).reverse().toString();
    }

    public static String f(String var0) {
        char[] var1 = var0.toCharArray();

        for(int var2 = 0; var2 < var1.length - 4; ++var2) {
            a(var1, var2, var2 + 2, 1);
            a(var1, var2 + 1, var2 + 3, 1);
        }

        return new String(var1);
    }

    public static String g(String var0) {
        char[] var1 = var0.toCharArray();

        for(int var2 = var1.length - 5; var2 >= 0; --var2) {
            a(var1, var2, var2 + 2, -1);
            a(var1, var2 + 1, var2 + 3, -1);
        }

        return new String(var1);
    }

    private static char a(Character var0, int var1) {
        if (Character.isUpperCase(var0)) {
            return Character.toLowerCase(var0);
        } else if (Character.isLowerCase(var0)) {
            return Character.toUpperCase(var0);
        } else {
            return Character.isDigit(var0) ? (char)(48 + (var0 - 48 + 10 + var1) % 10) : var0;
        }
    }

    public static void a(char[] var0, int var1, int var2, int var3) {
        char var4 = var0[var1];
        var0[var1] = a(var0[var2], var3);
        var0[var2] = a(var4, var3);
    }

}
