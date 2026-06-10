package aX;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import com.grapecity.documents.excel.internals.aY.b;
import com.grapecity.documents.excel.internals.bJ.ak;
import com.grapecity.documents.excel.internals.bJ.ce;


final class a {
    a() {
    }

    private static PublicKey a() {
        KeyFactory var0;
        try {
            var0 = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException var6) {
            b.a(4, var6);
            throw ak.a(var6);
        }

        byte[] var1;
        if (f.a()) {
            var1 = a("tgoYDy+InG+V+F4gU9ssbjuTHXzHaXwFzyF+SA85fe4AeN1N5jzxA2MzXT8VsArKZ9Ugz2rYPp9kOhpwiSq2QSfxE+axl8403O9JkcB9826e7Co3WjZYOMbfKWLrRJFTWatkEIRJvP2ocOEtYCLDOaET08OCAnSFAcO7fReSd00=");
        } else {
            var1 = a("+US+f7+816LsJEa/Gd0daNSWKehDM7uF72OpCHfSECbjX9WlrrOxIq8kOgIVxkDfJeWmP6OwZw9Xn+VJa8Sxze7jKUopRH1awfK1p+RiQnOcmqUpi4GTUHK+6F9nSc/Y0T5H7pgDNSk8CkB/LwfaaCj0FmrEXns8fguG2l3VrCU=");
        }

        byte[] var2 = a("AQAB");
        RSAPublicKeySpec var3 = new RSAPublicKeySpec(a(var1), a(var2));

        try {
            return var0.generatePublic(var3);
        } catch (InvalidKeySpecException var5) {
            b.a(4, 2);
            throw ak.a(var5);
        }
    }

    private static BigInteger a(byte[] var0) {
        return new BigInteger(1, var0);
    }

    public static boolean a(String var0, String var1) {
        PublicKey var2 = a();
        byte[] var3 = ce.c().b(var0);
        byte[] var4 = a(var1);

        Signature var5;
        try {
            var5 = Signature.getInstance("SHA256withRSA");
        } catch (NoSuchAlgorithmException var10) {
            b.a(3, 1, var10);
            throw ak.a(var10);
        }

        try {
            var5.initVerify(var2);
        } catch (InvalidKeyException var9) {
            b.a(3, 2);
            throw ak.a(var9);
        }

        try {
            var5.update(var3);
        } catch (SignatureException var8) {
            b.a(3, 3);
            throw ak.a(var8);
        }

        try {
            return var5.verify(var4);
        } catch (SignatureException var7) {
            b.a(3, 4);
            throw ak.a(var7);
        }
    }

    public static byte[] a(String var0) {
        if (bJ.bR.a(var0)) {
            return new byte[0];
        } else {
            if (var0.length() % 4 != 0) {
                int var1 = 4 - var0.length() % 4;
                var0 = var0 + bJ.bR.c('=', var1);
            }

            return bJ.h.a(var0);
        }
    }
}
