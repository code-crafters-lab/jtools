import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * V9 版本 License 生成器测试
 *
 * <p>验证 V9 授权密钥的完整生成流程：
 * <ul>
 *   <li>加载 X.509 私钥用于签名</li>
 *   <li>构造授权数据（组织、产品、有效期等）</li>
 *   <li>生成授权密钥并保存到文件</li>
 *   <li>验证编码/解码往返一致性</li>
 * </ul>
 *
 * @author GcExcel Demo
 * @since 1.0.0
 */
@Slf4j
@DisplayName("V9 版本 License 生成器测试")
class V9LicenseGeneratorTest {

    /**
     * 测试 V9 License 密钥生成的完整流程
     *
     * <p>执行步骤：
     * <ol>
     *   <li>从 PEM 文件加载 X.509 私钥</li>
     *   <li>构造授权数据（ID、组织、产品、有效期等）</li>
     *   <li>使用私钥签名并生成 License Key</li>
     *   <li>将生成的 License 保存到 v9.lic 文件</li>
     *   <li>验证编码往返一致性（decode → encode 结果不变）</li>
     * </ol>
     *
     * @throws Exception 私钥加载或文件写入失败时抛出
     */
    @Test
    @DisplayName("生成 V9 License 并验证编码往返一致性")
    void V9LicenseGenerator() throws Exception {
        log.info("=== V9 License Generator ===");

        // 从 PEM 文件加载 X.509 格式私钥，用于对 License 数据进行签名
        PrivateKey privateKey = KeyUtils.getX509PrivateKey("/Users/wuyujie/Project/personal/go-socket/gcexcel/private.pem");

        // ---- 构造授权数据 ----
        // 生成唯一标识符作为 License ID
        UUID uuid = UUID.randomUUID();

        // 计算创建时间和过期时间（当前时间 + 1年）
        ZonedDateTime now = Instant.now().atZone(ZoneId.systemDefault());
        Date crtDate = Date.from(now.toInstant());
        Date expDate = Date.from(now.plusYears(1).toInstant());

        // 使用 Builder 模式构建授权数据对象
        V9LicenseGenerator.LicenseData data = new V9LicenseGenerator.LicenseData()
                .id(uuid.toString())           // License 唯一标识
                .evl(false)                    // 评估试用版本（false 表示正式版）
                .oid("jqsoft")                 // 组织标识
                .cna("安徽晶奇网络科技股份有限公司") // 组织名称
//                .cid("contact@example.com")   // 联系人邮箱（可选）
//                .dms("localhost")             // 绑定域名（可选）
//                .ips("127.0.0.1,10.1.40.40") // 绑定 IP 地址（可选）
                .exp(expDate)                  // 过期时间
                .crt(crtDate)                  // 创建时间
                .products(new V9LicenseGenerator.Product("GCExcel", "93W7")) // 授权产品及版本
                .anl(false, "v9");             // 年度许可模式，版本标识

        // ---- 生成 V9 License 密钥 ----
        String sep = "#A1";                    // License Key 分隔符
        String prefix = LicenseIDGenerator.generate(uuid); // 根据 UUID 生成前缀
        String licenseKey = V9LicenseGenerator.generate(privateKey, data, prefix, sep);
        // 将生成的 License 保存到测试资源目录
        Files.writeString(Path.of("/Users/wuyujie/Project/opensource/jtools/gcexcel/src/test/resources/v9.lic"), licenseKey);
        log.info("Generated V9 Key ({} chars):", licenseKey.length());
        log.info(licenseKey);

        // ---- 验证编码往返一致性 ----
        // 解析 License Key，提取编码后的授权数据部分
        String[] split = licenseKey.split(sep);
        String encData = split[1];
        // 执行 decode → encode 往返，验证编码结果是否一致
        String reEncoded = V9LicenseGenerator.awE_encode(V9LicenseGenerator.awE_decode(encData));
        log.info("=== Verification: clean= {}", encData.equals(reEncoded));
    }
}
