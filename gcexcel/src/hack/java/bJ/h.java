package bJ;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class h implements Serializable {
    private h() {
    }

    public static byte[] a(byte[] var0) {
        return Base64.getDecoder().decode(var0);
    }

    public static byte[] a(String var0) {
        return a(var0.getBytes(StandardCharsets.UTF_8));
    }

    public static String b(byte[] var0) {
        return Base64.getEncoder().encodeToString(var0);
    }
}
