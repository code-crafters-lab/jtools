import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Slf4j
class V9LicenseGeneratorTest {

    @Test
    void V9LicenseGenerator() throws Exception {
        log.info("=== V9 License Generator ===");

        PrivateKey privateKey = KeyUtils.getX509PrivateKey("/Users/wuyujie/Project/personal/go-socket/gcexcel/private.pem");

        // ---- Build license data ----
        // Exp MUST be in the past so that canExpire=true (aV.e.b() returns true)
        // and the V9 license manager keeps the provider (aV.k.d != null).
        V9LicenseGenerator.LicenseData data = new V9LicenseGenerator.LicenseData()
                .id(UUID.randomUUID().toString())
                .evl(false) // 评估试用版本
                .oid("ORG-2024")
                .cna("CodeCraftersLab")
                .cid("contact@example.com")
//                .dms("localhost")
//                .ips("127.0.0.1,10.1.40.40")
//                .exp(new SimpleDateFormat("yyyyMMdd").parse("20260607"))
                .crt(new Date())
                .products(new V9LicenseGenerator.Product("GCExcel", "93W7"))
                .anl(false, "v9")
                ;

        // ---- Generate V9 license key ----
        String sep = "#A1";
        String licenseKey = V9LicenseGenerator.generate(privateKey, data,"c",sep);
        Files.writeString(Path.of("/Users/wuyujie/Project/opensource/jtools/gcexcel/src/test/resources/v9.lic"), licenseKey);
        log.info("Generated V9 Key ({} chars):", licenseKey.length());
        log.info(licenseKey);

        // ---- Verify full roundtrip ----
        String[] split = licenseKey.split(sep);
        String encData = split[1];
        String reEncoded = V9LicenseGenerator.awE_encode(V9LicenseGenerator.awE_decode(encData));
        log.info("=== Verification: clean= {}", encData.equals(reEncoded));
    }
}
