import java.nio.charset.StandardCharsets;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;

/**
 * V9 授权生成器 — 基于 GcExcel 9.0.1 逆向工程精确实现。
 * <p>
 * 生成流程：
 * <pre>
 * LicenseData → serializeJson() → buildSignData() → RSA sign → wrapJson()
 * → {aW,e}. encode() → 最终 key 字符串
 * </pre>
 * <p>
 */
public class V9LicenseGenerator {

    private static final Locale EMPTY_LOCALE = new Locale("", "", "");
    private static final SimpleDateFormat EXP_DATE_FMT = new SimpleDateFormat("yyyyMMdd", EMPTY_LOCALE);
    private static final SimpleDateFormat CRT_DATE_FMT = new SimpleDateFormat("yyyyMMdd hhmmss", EMPTY_LOCALE);

    static {
        EXP_DATE_FMT.setLenient(false);
        CRT_DATE_FMT.setLenient(false);
    }

    /**
     * V9 产品信息 (对应 aV.i)
     */
    public record Product(String name, String code) {
    }

    /**
     * V9 License 数据模型 (对应 aV.f + Anl 嵌套)。
     * <p>
     * 序列化为 JSON 后作为 "D" 字段嵌入外层包装 {@code {"S":"<签名>","D":<此 JSON>}}，
     * 再经 aW.e 4 层混淆编码得到最终 license key。
     * <p>
     * JSON 字段映射 (参见 GCEXCEL_LICENSE_ANALYSIS.md §4.8/4.10)：
     * <pre>
     * {
     *   "Id":  "license-uuid",
     *   "Evl": false,
     *   "OId": "org-id",
     *   "CNa": "company-name",
     *   "CId": "contact-id",
     *   "Dms": "*.example.com",
     *   "Ips": "192.168.1.*",
     *   "Exp": "2026-06-01",
     *   "Crt": "2026-01-01",
     *   "Prd": [{"N":"GcExcel","C":"GCEXCEL"}],
     *   "Anl": {"dsr":true, "ver": "v9"}
     * }
     * </pre>
     */
    public static class LicenseData {

        /** Id — License 唯一标识 (UUID) */
        String id;

        /** Evl — 试用评估标记，true 表示评估版/试用版 */
        boolean evl;

        /** OId — 组织/企业 ID (Organization ID) */
        String oid;

        /** CNa — 公司名称 (Company Name) */
        String cna;

        /** CId — 联系人 ID (Contact ID) */
        String cid;

        /** Dms — 域名绑定，分号分隔，用于限制授权绑定的域名 */
        String dms;

        /** Ips — IP 地址绑定，逗号分隔，用于限制授权绑定的 IP 段 */
        String ips;

        /** Exp — 过期时间，格式 yyyyMMdd；null 表示永不过期 */
        Date exp;

        /** Crt — 创建时间，格式 yyyyMMdd hhmmss */
        Date crt;

        /** Prd — 产品数组，每个元素包含产品名称和产品代码 (对应 aV.i) */
        Product[] products;

        /** Anl.dsr — 反序列化标记；null 时跳过整个 Anl 块 */
        Boolean anlDsr;

        /** Anl.ver — 版本号，如 "v9" */
        String anlVer;

        /** @param id License 唯一标识 (UUID) */
        public LicenseData id(String id) {
            this.id = id;
            return this;
        }

        /** @param evl 试用评估标记 */
        public LicenseData evl(boolean evl) {
            this.evl = evl;
            return this;
        }

        /** @param oid 组织/企业 ID */
        public LicenseData oid(String oid) {
            this.oid = oid;
            return this;
        }

        /** @param cna 公司名称 */
        public LicenseData cna(String cna) {
            this.cna = cna;
            return this;
        }

        /** @param cid 联系人 ID */
        public LicenseData cid(String cid) {
            this.cid = cid;
            return this;
        }

        /** @param dms 域名绑定，分号分隔 */
        public LicenseData dms(String dms) {
            this.dms = dms;
            return this;
        }

        /** @param ips IP 地址绑定，逗号分隔 */
        public LicenseData ips(String ips) {
            this.ips = ips;
            return this;
        }

        /** @param exp 过期时间，null 表示永不过期 */
        public LicenseData exp(Date exp) {
            this.exp = exp;
            return this;
        }

        /** @param crt 创建时间 */
        public LicenseData crt(Date crt) {
            this.crt = crt;
            return this;
        }

        /** @param products 产品列表 */
        public LicenseData products(Product... products) {
            this.products = products;
            return this;
        }

        /** @param dsr 反序列化标记 (null 跳过 Anl)；@param ver 版本号 */
        public LicenseData anl(Boolean dsr, String ver) {
            this.anlDsr = dsr;
            this.anlVer = ver;
            return this;
        }
    }

    /**
     * 生成 V9 License key 字符串
     *
     * @param privateKey RSA 私钥 (用于签名)
     * @param data       License 数据
     * @param prefix     前缀字符 (默认 "c")
     * @param separator  分隔符 (默认 "#A1")
     * @return 完整的 V9 license key 字符串
     */
    public static String generate(PrivateKey privateKey, LicenseData data,
                                  String prefix, String separator) throws Exception {
        // 1. 序列化 license 数据为 JSON (精确匹配 aW.c.a(aV.f))
        String innerJson = serializeLicenseFields(data);

        // 2. 构建待签名数据: prefix + separator + innerJson
        String signData = String.format("%s%s%s", prefix, separator, innerJson);

        // 3. RSA 签名 (SHA256withRSA)
        Signature sig = Signature.getInstance("SHA256WithRSA");
        sig.initSign(privateKey);
        sig.update(signData.getBytes(StandardCharsets.UTF_8));
        byte[] signatureBytes = sig.sign();
        String signatureB64 = Base64.getEncoder().encodeToString(signatureBytes);

        // 4. 构建外层包装 JSON: {"S":"<signature>","D":<innerJson>}
        String outerJson = "{\"S\":\"" + signatureB64 + "\",\"D\":" + innerJson + "}";
        outerJson = String.format("{\"S\":\"%s\",\"D\":%s}", signatureB64, innerJson);

        // 5. 应用 aW.e 混淆编码
        String encoded = awE_encode(outerJson);

        // 6. 组装最终 key: <prefix><separator><encoded>
        return prefix + separator + encoded;
    }

    /**
     * 简化调用: 默认使用前缀 "c" 和分隔符 "#A1"
     */
    public static String generate(PrivateKey privateKey, LicenseData data) throws Exception {
        return generate(privateKey, data, "c", "#A0");
    }

    // ============================================================
    //  精确实现 aW.c.a(aV.f) — 手动 JSON 序列化
    // ============================================================

    static String serializeLicenseFields(LicenseData d) {
        if (d == null) return "";

        StringBuilder sb = new StringBuilder("{");

        // 1. Anl 块 (仅当 anlDsr != null)
        if (d.anlDsr != null) {
            sb.append("\"Anl\":{");
            sb.append("\"dsr\":").append(d.anlDsr.toString().toLowerCase(EMPTY_LOCALE));
            if (d.anlVer != null && !d.anlVer.isEmpty()) {
                sb.append(",\"ver\":\"").append(d.anlVer.toLowerCase(EMPTY_LOCALE)).append("\"");
            }
            sb.append("},");
        }

        // 2. Id
        sb.append("\"Id\":\"").append(d.id != null ? d.id : "").append("\",");

        // 3. Evl (仅当 true)
        if (d.evl) {
            sb.append("\"Evl\":").append(Boolean.toString(true).toLowerCase(EMPTY_LOCALE)).append(",");
        }

        // 4. OId
        appendIfNotEmpty(sb, "OId", d.oid);

        // 5. CNa
        appendIfNotEmpty(sb, "CNa", d.cna);

        // 6. CId
        appendIfNotEmpty(sb, "CId", d.cid);

        // 7. Dms
        appendIfNotEmpty(sb, "Dms", d.dms);

        // 8. Ips
        appendIfNotEmpty(sb, "Ips", d.ips);

        // 9. Exp
        if (d.exp != null) {
            sb.append("\"Exp\":\"").append(EXP_DATE_FMT.format(d.exp)).append("\",");
        }

        // 10. Crt
        sb.append("\"Crt\":\"").append(CRT_DATE_FMT.format(d.crt)).append("\",");

        // 11. Prd
        sb.append("\"Prd\":[");
        if (d.products != null) {
            for (int i = 0; i < d.products.length; i++) {
                if (i > 0) sb.append(",");
                Product p = d.products[i];
                sb.append("{\"N\":\"").append(p.name()).append("\",\"C\":\"").append(p.code()).append("\"}");
            }
        }
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }

    private static void appendIfNotEmpty(StringBuilder sb, String key, String value) {
        if (value != null && !value.isEmpty()) {
            sb.append("\"").append(key).append("\":\"").append(value).append("\",");
        }
    }

    // ============================================================
    //  精确实现 aW.e — 混淆编码流水线
    // ============================================================

    /**
     * 编码流水线 (与 JAR 中 aW.e.a 字节码一致 — 名称反命名)
     * <p>
     * JAR 字节码顺序: c → replace("==","&") → replace("=","#") → a(., true) → e → f
     */
    static String awE_encode(String plaintext) {
        // 1. c: UTF-8 → Base64 编码
        String s = Base64.getEncoder().encodeToString(plaintext.getBytes(StandardCharsets.UTF_8));
        // 2. 替换 padding: == → &, = → #
        s = s.replace("==", "&").replace("=", "#");
        // 3. a(., true): 旋转 (reverse=true → 奇数 len 时不 +1)
        s = rotate(s, true);
        // 4. e: 反转字符串
        s = new StringBuilder(s).reverse().toString();
        // 5. f: 正向交换字符对 (i 从 0 递增到 len-5)
        s = swapPairsForward(s);
        return s;
    }

    /**
     * 解码流水线 (与 JAR 中 aW.e.b 字节码一致 — 名称反命名)
     * <p>
     * JAR 字节码顺序: g → e → a(., false) → replace("#","=") → replace("&","==") → d
     */
    static String awE_decode(String encoded) {
        // 1. g: 逆向交换字符对 (i 从 len-5 递减到 0)
        String s = swapPairsBackward(encoded);
        // 2. e: 反转字符串
        s = new StringBuilder(s).reverse().toString();
        // 3. a(., false): 旋转 (reverse=false → 奇数 len 时 +1)
        s = rotate(s, false);
        // 4. 替换: # → =, & → ==
        s = s.replace("#", "=").replace("&", "==");
        // 5. d: Base64 解码 → UTF-8
        s = new String(Base64.getDecoder().decode(s.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        return s;
    }

    /**
     * aW.e.a(String, boolean) — 旋转
     * <p>
     * encode (dir=true): 分割点 = len/2, 右半部分移到前面
     * decode (dir=false): 偶数 len/2, 奇数 len/2+1, 右半部分移到前面
     */
    private static String rotate(String s, boolean direction) {
        int len = s.length();
        int split = len / 2;
        if (len % 2 != 0 && !direction) {
            split += 1;
        }
        return s.substring(split) + s.substring(0, split);
    }

    /**
     * aW.e.f(String) — 正向交换 (编码)
     */
    private static String swapPairsForward(String s) {
        char[] arr = s.toCharArray();
        for (int i = 0; i < arr.length - 4; i++) {
            swapTransform(arr, i, i + 2, 1);
            swapTransform(arr, i + 1, i + 3, 1);
        }
        return new String(arr);
    }

    /**
     * aW.e.g(String) — 逆向交换 (解码)
     */
    private static String swapPairsBackward(String s) {
        char[] arr = s.toCharArray();
        int start = arr.length - 5;
        for (int i = start; i >= 0; i--) {
            swapTransform(arr, i, i + 2, -1);
            swapTransform(arr, i + 1, i + 3, -1);
        }
        return new String(arr);
    }

    /**
     * aW.e.a(char[], int, int, int) — 交换 + 变换
     */
    private static void swapTransform(char[] arr, int i, int j, int dir) {
        char temp = arr[i];
        arr[i] = transformChar(arr[j], dir);
        arr[j] = transformChar(temp, dir);
    }

    /**
     * aW.e.a(Character, int) — 字符变换:
     * 大写→小写, 小写→大写, 数字: c ± 1 (mod 10), 其他不变
     */
    private static char transformChar(char c, int dir) {
        if (Character.isUpperCase(c)) return Character.toLowerCase(c);
        if (Character.isLowerCase(c)) return Character.toUpperCase(c);
        if (Character.isDigit(c)) {
            return (char) ('0' + ((c - '0') + 10 + dir) % 10);
        }
        return c;
    }

    // ============================================================
    //  验证: 确保生成的 key 可以被 V9 解码
    // ============================================================

    /**
     * 验证 key: 解码混淆层并提取 signing data (用于调试)
     */
    public static String debugDecode(String licenseKey) throws Exception {
        int sepIdx = licenseKey.indexOf("#A1");
        if (sepIdx < 0) sepIdx = licenseKey.indexOf("#A0");
        if (sepIdx < 0) throw new IllegalArgumentException("无法找到分隔符 #A1 或 #A0");

        String data = licenseKey.substring(sepIdx + 3);
        String decoded = awE_decode(data);
        System.out.println("解码后的 JSON:\n" + decoded);
        return decoded;
    }

}
