import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class LicenseMange {

    private static String BASE = "/Users/wuyujie/.local/share";

    public static String GetLicense() {
        try {
            Path path = Path.of(BASE, "GrapeCity", "6bf630ea-22d3-47b5-bb9e-2102f3c52186", ".license");
            path = Path.of("/Users/wuyujie/Project/personal/go-socket/gcexcel",".license");
            byte[] bytes = Files.readAllBytes(path);
            String s = new String(bytes, StandardCharsets.UTF_8);
            log.info("license: {}", s);
            return s;
        } catch (IOException e) {
            log.error("读取license失败", e);
            return null;
        }
    }

    public static Object parse(String var0) {
        if (var0 == null || var0.isEmpty()) {
            return null;
        } else {
            String[] result = var0.split(";");

            boolean s = verify(result[0], result[1]);
            if (!s) {
                throw new RuntimeException("license校验失败");
            }

            String[] var1 = result[0].split(",", -1);
            List<String> collect = Arrays.stream(var1).map(LicenseMange::d).toList();
            if (var1.length != 11) {
                return null;
            } else {
                License lic = new License();
                lic.uuid = UUID.fromString(d(var1[0]));
                lic.serialNumber = d(var1[1]);
                lic.hostname = d(var1[2]);
                lic.a = Boolean.parseBoolean(d(var1[3]));
                lic.activeTime = Integer.parseInt(d(var1[4]));
                lic.f = Boolean.parseBoolean(d(var1[5]));
                lic.expiryTime = Integer.parseInt(d(var1[6]));
                lic.version = d(var1[8]);
                lic.i = d(var1[9]);
                lic.j = d(var1[10]);
                return lic;
            }
        }
    }

    private static PublicKey getPubKey(boolean lmTest) {
        PublicKey publicKey = null;

        byte[] modulusBytes;
        if (lmTest) {
            modulusBytes = decodeBytes("tgoYDy+InG+V+F4gU9ssbjuTHXzHaXwFzyF+SA85fe4AeN1N5jzxA2MzXT8VsArKZ9Ugz2rYPp9kOhpwiSq2QSfxE+axl8403O9JkcB9826e7Co3WjZYOMbfKWLrRJFTWatkEIRJvP2ocOEtYCLDOaET08OCAnSFAcO7fReSd00=");
            modulusBytes = decodeBytes("AKr5BPhLiCKb3Rc0ZUVFMpSQUYX8CVac2akqS+C24k9eHgLmcTUcmWsuGrPbaAE6uxkEw7LPFJb4EtF0/YdWG7MsJZVOzC7fQ44+nt2L1SOwan5ZFLXlychnLi6VWMdB8d20Trcrq483JtTpWj+Af3rdnEecxijKK6PKDQVXKJPUKg31pHUQBeVUoLJaUpDJtJAyHXp8bY0OUMGp8GCnoF8UPOkCHLtbsx8VrMezfNFoWzoad3Dvg85ebUDJN0qsnmv7V9p+BgiOcuUzVdJ3Xnnv9PVsjm9bm5dWu//NcdrdErIMSpMqZWwaO3KppEokYni5BEvM69jfY5//XRCAYW0=");
        } else {
            modulusBytes = decodeBytes("+US+f7+816LsJEa/Gd0daNSWKehDM7uF72OpCHfSECbjX9WlrrOxIq8kOgIVxkDfJeWmP6OwZw9Xn+VJa8Sxze7jKUopRH1awfK1p+RiQnOcmqUpi4GTUHK+6F9nSc/Y0T5H7pgDNSk8CkB/LwfaaCj0FmrEXns8fguG2l3VrCU=");
            modulusBytes = decodeBytes("AJdDILyIft6d5cnu8khv06/SU4TYJhcfs090NvgchAySu0F3MvVA0ZxeIsSeun978CJfxY9DBlUlb3ReQzHlTiYD4SwoXtQ15wXEfR56sKNk1nrfmZ+nwej9G+n4ZIKGLwG9ikCiLqbgifYVWW0tm2Euxt81c9CDmMxCygSezpQOQNoP6zRWd+KlT6P5TQ7AVGqSgfr1qLQpQ6xjkw+s0UnHE5a4jWQim9E6k5HV/0P5X9yIv+vL3dh97hSTEpacyMzqD3o47Y9mvXjg+rE5XV7Zj0xQ2YVsgX0aqrsiiwSGUL2Mu42p7j0CBnVaSAuWNgh1v34OGqNMNdpdBfZGT8U=");
        }

        byte[] exponentBytes = decodeBytes("AQAB");
        BigInteger n = new BigInteger(1, modulusBytes);
        BigInteger e = new BigInteger(1, exponentBytes);
        RSAPublicKeySpec var3 = new RSAPublicKeySpec(n, e);

        try {
            KeyFactory var0 = KeyFactory.getInstance("RSA");
            publicKey = var0.generatePublic(var3);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException exception) {
            log.error(exception.getMessage(), exception);
        }
        return publicKey;
    }

    public static boolean verify(String source, String sign) {
        PublicKey var2 = getPubKey(false);
        byte[] var3 = new byte[0];
        if (source != null && !source.isEmpty()) {
            var3 = a(CharBuffer.wrap(source), StandardCharsets.UTF_8);
        }
        byte[] var4 = decodeBytes(sign);

        Signature var5;
        try {
            var5 = Signature.getInstance("SHA256WithRSA");
            var5.initVerify(var2);
            var5.update(var3);
            boolean verify = var5.verify(var4);
            log.info("verify result: {}", verify);
            return verify;
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException var10) {
            log.error("verify error", var10);
        }

        return false;
    }

    private static byte[] a(CharBuffer var0, Charset var1) {
        if (var0 != null && !var0.isEmpty()) {
            ByteBuffer var2 = var1.encode(var0);
            if (var2.hasArray()) {
                byte[] var7 = var2.array();
                int var8 = var2.arrayOffset();
                int var5 = var2.limit();
                if (var8 == 0 && var5 == var7.length) {
                    return var7;
                } else {
                    byte[] var6 = new byte[var5];
                    System.arraycopy(var7, var8, var6, 0, var5);
                    return var6;
                }
            } else {
                int var3 = var2.limit();
                byte[] var4 = new byte[var3];
                var2.get(var4);
                return var4;
            }
        } else {
            return new byte[0];
        }
    }

    public static String d(String var0) {
        if (var0 == null || var0.isEmpty()) {
            return "";
        }
        byte[] var1 = decodeBytes(var0);
        String s = new String(var1, StandardCharsets.UTF_8);
//        com.grapecity.documents.excel.internals.bJ.ce.c().a(com.grapecity.documents.excel.internals.aX.a.a(var0));
        return s;
    }

    public static byte[] decodeBytes(String var0) {
        if (var0 == null || var0.isEmpty()) {
            return new byte[0];
        } else {
            if (var0.length() % 4 != 0) {
                int var1 = 4 - var0.length() % 4;
                var0 = var0 + bRc('=', var1);
            }
            byte[] bytes = var0.getBytes(StandardCharsets.UTF_8);
            return Base64.getDecoder().decode(bytes);
        }
    }

    public static String bRc(char var0, int var1) {
        StringBuilder var2 = new StringBuilder();

        for (int var3 = 0; var3 < var1; ++var3) {
            var2.append(var0);
        }

        return var2.toString();
    }

    private static class License {
        private boolean a;
        private UUID uuid;
        private String serialNumber;
        private String hostname;
        private int activeTime;
        private boolean f;
        private int expiryTime;
        private String version; // Unlimited
        private String i;
        private String j;

        private License() {
        }
    }

    /**
     * 读取 PEM 公钥文件，转换为 PublicKey 对象
     *
     * @param pemPublicKeyPath PEM 公钥文件路径（如 "./rsa_public.pem"）
     * @return RSA PublicKey 对象
     */
    public static RSAPublicKey getX509PublicKey(String pemPublicKeyPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        // 1. 读取 PEM 文件内容，拼接核心的 Base64 字符串（去除头尾标识和换行符）
        String pem = Files.readAllLines(Paths.get(pemPublicKeyPath))
                .stream().filter(line ->
                        !line.startsWith("-----BEGIN PUBLIC KEY-----")
                                && !line.startsWith("-----END PUBLIC KEY-----")
                                && !line.isEmpty())
                .collect(Collectors.joining());

        // 2. Base64 解码，将字符串转换为二进制 DER 数据
        byte[] publicKeyDer = Base64.getDecoder().decode(pem);

        // 3. 构造 X.509 编码的公钥规格
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKeyDer);

        // 4. 实例化 RSA 密钥工厂，转换为 PublicKey 对象
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(x509KeySpec);

        // 可选：转换为 RSAPublicKey，获取公钥的模数、指数等详细信息
        return (RSAPublicKey) publicKey;
    }
}
