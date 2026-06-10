package aW;

import bJ.h;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;

public final class d {
    Signature a = Signature.getInstance("SHA256withRSA");
    private int b = 2048;
    private String c;
    private String d;

    public d(String var1, String var2) throws Exception {
        this.c = var1;
        this.d = var2;
    }

    public boolean a(String var1, String var2) throws Exception {
        if (var2.isEmpty()) {
            return false;
        } else {
            try {
                byte[] var3 = h.a(this.c.getBytes(StandardCharsets.UTF_8));
                int var4 = var3.length;
                if (var4 * 8 != this.b) {
                    throw new IllegalArgumentException("RSA modulus length (" + var4 * 8 + " bits) does not match expected key size " + this.b + " bits.");
                } else {
                    BigInteger var5 = new BigInteger(1, var3);
                    BigInteger var6 = new BigInteger(1, h.a(this.d.getBytes(StandardCharsets.UTF_8)));
                    KeyFactory var7 = KeyFactory.getInstance("RSA");
                    RSAPublicKeySpec var8 = new RSAPublicKeySpec(var5, var6);
                    RSAPublicKey var9 = (RSAPublicKey)var7.generatePublic(var8);
                    this.a.initVerify(var9);
                    byte[] var10 = var1.getBytes(StandardCharsets.UTF_8);
                    this.a.update(var10);
                    byte[] var11 = h.a(var2.getBytes());
                    return this.a.verify(var11);
                }
            } catch (IllegalArgumentException var12) {
                throw new Exception(var12.getMessage());
            } catch (Exception var13) {
                throw new Exception("Configuration Errors, signature object is not initialized properly");
            }
        }
    }
}
