import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

public class LicenseIDGenerator {

    // JetBrains LicenseID 字符池（大写字母+数字）
    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int POOL_LEN = CHAR_POOL.length();
    // 固定输出10位
    private static final int OUT_LEN = 10;

    /**
     * 根据UUID生成固定10位唯一License ID
     * @param uuid 标准UUID字符串 / UUID对象
     * @return 10位大写字母数字LicenseID
     */
    public static String generate(UUID uuid) {
        return generate(uuid.toString());
    }

    public static String generate(String uuidStr) {
        try {
            // 1. SHA256哈希UUID字符串，得到固定摘要
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] sha256Bytes = md.digest(uuidStr.getBytes(StandardCharsets.UTF_8));

            long mix = 0;
            // 混合全部32字节，打散全局特征
            for (byte b : sha256Bytes) {
                mix = mix * 31 + (b & 0xFF);
            }
            StringBuilder sb = new StringBuilder(OUT_LEN);
            // 循环从完整摘要取字符，而不是仅前10字节
            for (int i = 0; i < OUT_LEN; i++) {
                int pos = (int) ((mix + sha256Bytes[(i * 7) % 32]) & 0xFF);
                int idx = pos % POOL_LEN;
                sb.append(CHAR_POOL.charAt(idx));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("生成LicenseID失败", e);
        }
    }
}
