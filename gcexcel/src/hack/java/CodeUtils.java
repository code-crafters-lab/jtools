import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * 哈希短码生成工具类
 *
 * <p>提供基于 SHA-256 的固定长度字母数字短码生成能力。
 * 短码仅包含 {@code A-Z} 和 {@code 0-9}，适用于文件名、唯一标识等
 * 需要紧凑可读编码的场景
 *
 * <p>核心特性：
 * <ul>
 *   <li>相同输入始终产生相同输出，可作为稳定的派生标识</li>
 *   <li>全部摘要字节参与计算，信息利用率 100%</li>
 *   <li>输出长度可调，默认 10 位</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * String code = CodeUtils.sha256ShortCode("file-content");
 * // 输出类似 "O2LMSDO6IH"，固定 10 位大写字母数字
 *
 * String shortCode = CodeUtils.sha256ShortCode("data", 6);
 * // 输出 6 位短码
 *
 * byte[] fileBytes = Files.readAllBytes(path);
 * String fileCode = CodeUtils.sha256ShortCode(fileBytes);
 * // 直接对字节数组生成短码，适用于文件内容哈希
 * }</pre>
 *
 * @see #sha256ShortCode(String)
 * @see #sha256ShortCode(String, int)
 * @see #sha256ShortCode(byte[])
 * @see #sha256ShortCode(byte[], int)
 */
public final class CodeUtils {

    /**
     * 输出字符集：大写字母 A-Z 加数字 0-9，共 36 个字符
     */
    private static final String ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * 字符集长度，用于取模运算
     */
    private static final int ALPHANUMERIC_LEN = ALPHANUMERIC_CHARS.length();

    /**
     * 默认短码长度（10 位），提供约 36^10 ≈ 3.6 × 10^15 种组合
     */
    private static final int DEFAULT_SHORT_CODE_LEN = 10;

    /**
     * SHA-256 算法名称
     */
    private static final String SHA256_ALGORITHM = "SHA-256";

    /**
     * 私有构造方法，防止实例化
     */
    private CodeUtils() {
    }

    /**
     * 对输入字符串计算 SHA-256 哈希并输出固定 10 位大写字母数字短码
     *
     * <p>等价于 {@code sha256ShortCode(input, 10)}
     *
     * @param input 输入字符串（不可为 null）
     * @return 固定 10 位大写字母数字短码，例如 {@code "O2LMSDO6IH"}
     * @throws NullPointerException 当 {@code input} 为 null 时
     * @see #sha256ShortCode(String, int)
     */
    public static String sha256ShortCode(String input) {
        return sha256ShortCode(input, DEFAULT_SHORT_CODE_LEN);
    }

    /**
     * 对输入字符串计算 SHA-256 哈希并输出指定长度的大写字母数字短码
     *
     * <p>内部流程：
     * <ol>
     *   <li>将输入字符串按 UTF-8 编码后计算 SHA-256 哈希，得到 32 字节摘要</li>
     *   <li>通过 {@link #digestToShortCode(byte[], int)} 将摘要折叠映射为指定长度的短码</li>
     * </ol>
     *
     * @param input 输入字符串（不可为 null）
     * @param len   输出短码长度，必须满足 {@code 0 < len ≤ 32}（SHA-256 摘要长度）
     * @return 大写字母数字短码
     * @throws NullPointerException      当 {@code input} 为 null 时
     * @throws IllegalArgumentException  当 {@code len} 不在合法范围时
     * @see #digestToShortCode(byte[], int)
     */
    public static String sha256ShortCode(String input, int len) {
        Objects.requireNonNull(input, "input must not be null");
        return digestToShortCode(digest(input), len);
    }

    /**
     * 对字节数组计算 SHA-256 哈希并输出固定 10 位大写字母数字短码
     *
     * <p>适用于已有原始字节（如文件内容、网络数据）的场景，
     * 避免不必要的字符串转换开销
     *
     * @param data 输入字节数组（不可为 null）
     * @return 固定 10 位大写字母数字短码
     * @throws NullPointerException 当 {@code data} 为 null 时
     * @see #sha256ShortCode(byte[], int)
     */
    public static String sha256ShortCode(byte[] data) {
        return sha256ShortCode(data, DEFAULT_SHORT_CODE_LEN);
    }

    /**
     * 对字节数组计算 SHA-256 哈希并输出指定长度的大写字母数字短码
     *
     * <p>内部直接对字节数组计算 SHA-256 摘要，无需经过字符串编码转换
     *
     * @param data 输入字节数组（不可为 null）
     * @param len  输出短码长度，必须满足 {@code 0 < len ≤ 32}
     * @return 大写字母数字短码
     * @throws NullPointerException     当 {@code data} 为 null 时
     * @throws IllegalArgumentException 当 {@code len} 不在合法范围时
     * @see #digestToShortCode(byte[], int)
     */
    public static String sha256ShortCode(byte[] data, int len) {
        Objects.requireNonNull(data, "data must not be null");
        return digestToShortCode(digest(data), len);
    }

    /**
     * 对输入字符串计算 SHA-256 摘要
     *
     * <p>使用 UTF-8 编码将字符串转换为字节数组后计算哈希，
     * 返回固定 32 字节的 SHA-256 摘要
     *
     * @param input 输入字符串
     * @return 32 字节的 SHA-256 摘要
     * @throws IllegalStateException 当 SHA-256 算法不可用时（理论上不会发生）
     */
    private static byte[] digest(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance(SHA256_ALGORITHM);
            return md.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * 对字节数组计算 SHA-256 摘要
     *
     * @param data 输入字节数组
     * @return 32 字节的 SHA-256 摘要
     * @throws IllegalStateException 当 SHA-256 算法不可用时
     */
    private static byte[] digest(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance(SHA256_ALGORITHM);
            return md.digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * 将字节摘要折叠映射为指定长度的字母数字短码
     *
     * <p>算法分为两个阶段：
     * <ol>
     *   <li><b>分桶累积</b>：将摘要的全部字节按 {@code i % len} 轮转分配到 {@code len} 个桶中，
     *       每个桶通过 {@code hash * 31 + byte} 公式累积混合。乘数 31 与 Java {@link String#hashCode()}
     *       使用相同的散列因子，能有效打散连续字节的模式。每个字节恰好落入一个桶，
     *       确保全部摘要信息被利用</li>
     *   <li><b>字符映射</b>：将每个桶的累积值通过 {@code & 0x7FFFFFFF} 取绝对值后
     *       对字符集长度取模，映射到 {@code A-Z0-9} 字符集中的对应字符</li>
     * </ol>
     *
     * <p>以 SHA-256 的32字节摘要、输出10位短码为例，每个桶接收3或4个字节，
     * 分布均匀且混合充分
     *
     * @param digest 字节摘要（通常为 SHA-256 的 32 字节输出）
     * @param len    输出字符长度，必须满足 {@code 0 < len ≤ digest.length}
     * @return 字母数字短码
     * @throws IllegalArgumentException 当 {@code len} 不在合法范围时
     */
    static String digestToShortCode(byte[] digest, int len) {
        if (len <= 0 || len > digest.length) {
            throw new IllegalArgumentException(
                    String.format("len must be in (0, %d], but was %d", digest.length, len));
        }
        // 分桶累积：每个字节按轮转索引 i % len 归入对应桶，通过乘法混合累积
        int[] buckets = new int[len];
        for (int i = 0; i < digest.length; i++) {
            buckets[i % len] = buckets[i % len] * 31 + Byte.toUnsignedInt(digest[i]);
        }
        // 字符映射：将桶值取绝对值后对字符集长度取模，转换为对应字符
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(ALPHANUMERIC_CHARS.charAt((buckets[i] & 0x7FFFFFFF) % ALPHANUMERIC_LEN));
        }
        return sb.toString();
    }

}
