package bJ;

public class bR {

    public static boolean a(String var0) {
        return var0 == null || var0.isEmpty();
    }

    public static boolean a(String var0, String var1) {
        if (var0 == null && var1 == null) {
            return true;
        } else {
            return var0 != null && var0.equals(var1);
        }
    }

    public static String a(String var0, String[] var1) {
        return var1 == null ? null : a(var0, (String[])var1, 0, var1.length);
    }

    public static String a(String var0, String[] var1, int var2, int var3) {
        String var4 = "";
        if (var1 == null) {
            return null;
        } else {
            for(int var5 = var2; var5 < var1.length && var5 - var2 < var3; ++var5) {
                if (var0 != null && var5 > var2) {
                    var4 = var4 + var0;
                }

                if (var1[var5] != null) {
                    var4 = var4 + var1[var5];
                }
            }

            return var4;
        }
    }

    public static String c(char var0, int var1) {
        StringBuilder var2 = new StringBuilder();

        for(int var3 = 0; var3 < var1; ++var3) {
            var2.append(var0);
        }

        return var2.toString();
    }


}
