package org.codecrafterslab.agent.plugin;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * RSA 密钥对指纹生成器
 *
 * <p>用于生成 BigInteger 二元组 (N, E) 的唯一 SHA-256 指纹
 * 应用于密钥识别、证书比对等场景
 */
public class PairFinger {

    /**
     * 十六进制字符映射表
     */
    private static final char[] HEX = "0123456789abcdef".toCharArray();

    /**
     * 生成二元 BigInteger (N, E) 的 SHA-256 唯一指纹
     *
     * <p>将 N 和 E 的字节表示通过长度前缀拼接后计算 SHA-256 哈希
     * 长度前缀防止不同字节边界碰撞，确保相同 (N,E) 永远产生相同指纹
     *
     * @param n RSA 模数
     * @param e RSA 公钥指数
     * @return 64 位小写十六进制指纹字符串
     * @throws IllegalArgumentException 如果 n 或 e 为 null
     */
    public static String sha256Hex(BigInteger n, BigInteger e) {
        if (n == null || e == null) {
            throw new IllegalArgumentException("n and e must not be null");
        }

        byte[] nb = n.toByteArray();
        byte[] eb = e.toByteArray();

        // 长度前缀 + 原始字节，避免不同值因字节边界不同而碰撞
        ByteBuffer buf = ByteBuffer.allocate(4 + nb.length + 4 + eb.length).order(ByteOrder.BIG_ENDIAN);
        buf.putInt(nb.length);
        buf.put(nb);
        buf.putInt(eb.length);
        buf.put(eb);

        MessageDigest sha;
        try {
            sha = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
        byte[] digest = sha.digest(buf.array());

        return toHex(digest);
    }

    public static String sha256HexFromBase64(String modulus) {
        BigInteger n = new BigInteger(1, Base64.getDecoder().decode(modulus.getBytes(StandardCharsets.UTF_8)));
        BigInteger e = new BigInteger("65537");
        return sha256Hex(n, e);
    }

    /**
     * 将字节数组转换为小写十六进制字符串
     *
     * @param bytes 待转换的字节数组
     * @return 十六进制字符串，长度为字节数组长度的两倍
     */
    private static String toHex(byte[] bytes) {
        char[] hex = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            hex[i * 2] = HEX[(bytes[i] >> 4) & 0x0F];
            hex[i * 2 + 1] = HEX[bytes[i] & 0x0F];
        }
        return new String(hex);
    }
}
