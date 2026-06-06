import com.grapecity.documents.excel.internals.bJ.bO;
import com.grapecity.documents.excel.internals.bJ.bR;
import lombok.extern.slf4j.Slf4j;
import org.codecrafterslab.agent.plugin.PairFinger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

@Slf4j
class LicenseMangeTest {

    @Test
    public void getPubKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        RSAPublicKey publicKey = LicenseMange.getX509PublicKey("./src/main/resources/public.pem");
        log.info("{}", "");
        log.debug("公钥模数 n (B10): {}", publicKey.getModulus());
        log.debug("公钥指数 e (B10): {}", publicKey.getPublicExponent());
        log.debug("公钥模数 n (B16): {}", publicKey.getModulus().toString(16));
        log.debug("公钥指数 e (B16): {}", publicKey.getPublicExponent().toString(16));
        log.debug("公钥模数 n (B64): {}", Base64.getEncoder().encodeToString(publicKey.getModulus().toByteArray()));
        log.debug("公钥指数 e (B64): {}", Base64.getEncoder().encodeToString(publicKey.getPublicExponent().toByteArray()));

        log.info("{}", "");
        RSAPublicKey testPublicKey = LicenseMange.getX509PublicKey("./src/main/resources/test_public.pem");
        log.debug("公钥模数 n (B10): {}", testPublicKey.getModulus());
        log.debug("公钥指数 e (B10): {}", testPublicKey.getPublicExponent());
        log.debug("公钥模数 n (B16): {}", testPublicKey.getModulus().toString(16));
        log.debug("公钥指数 e (B16): {}", testPublicKey.getPublicExponent().toString(16));
        log.debug("公钥模数 n (B64): {}", Base64.getEncoder().encodeToString(testPublicKey.getModulus().toByteArray()));
        log.debug("公钥指数 e (B64): {}", Base64.getEncoder().encodeToString(testPublicKey.getPublicExponent().toByteArray()));


        String m = "udkL9Yp1FotxuamEH+Q3l7d6r+PAhQ6YypLjvRCINZ19FKWe4DiN4VCRkxrP4kPaubPzoi0WNHThfTzrc8PToRuzQmbCxsvb1IsS6rOssOjFVvT09InQw5jogiY9nnVnA0lgmHd43KY1jvkiLV/iK+h4n0tz9w9D812eL6RVOYKzeN+uXsdOxrS01D+liBPx3UGfcNYW/jY5me7OQQPB5I16zvmifWqr68EVuRchI5xc+nDzu6lZghrMl33Lsbx8JljQiXmUi2PGSdXmj6adqPjeyLdHlPWuFMUEljYgVgaxAKxGBNyPm08lVMFnTpJUpQLB9Inc51itnFrob9wpEQ==";
        BigInteger var5 = new BigInteger(1, Base64.getDecoder().decode(m.getBytes(StandardCharsets.UTF_8)));
        BigInteger var6 = new BigInteger(1, Base64.getDecoder().decode("AQAB".getBytes(StandardCharsets.UTF_8)));

        KeyFactory var7 = KeyFactory.getInstance("RSA");
        RSAPublicKeySpec var8 = new RSAPublicKeySpec(var5, var6);
        String key = PairFinger.sha256Hex(var5, var6);
        log.info("{}", key);
        RSAPublicKey v9PublicKey = (RSAPublicKey) var7.generatePublic(var8);
        log.debug("公钥模数 n (B10): {}", v9PublicKey.getModulus());
        log.debug("公钥指数 e (B10): {}", v9PublicKey.getPublicExponent());
        log.debug("公钥模数 n (B16): {}", v9PublicKey.getModulus().toString(16));
        log.debug("公钥指数 e (B16): {}", v9PublicKey.getPublicExponent().toString(16));
        log.debug("公钥模数 n (B64): {}", Base64.getEncoder().encodeToString(testPublicKey.getModulus().toByteArray()));
        log.debug("公钥指数 e (B64): {}", Base64.getEncoder().encodeToString(testPublicKey.getPublicExponent().toByteArray()));

    }


    @Test
    void licenseParse() {
        String var0 = System.getenv("GCLMTEST");
        boolean isTest = bR.d(var0, "true", bO.d);
        log.debug("result: {}", isTest);
        // 是否破解授权
        boolean hack = false;

        String license = LicenseMange.GetLicense();
        if (!hack) {
            license = "NmJmNjMwZWEtMjJkMy00N2I1LWJiOWUtMjEwMmYzYzUyMTg2,NjA2NDExMDdYWFhYWFhYWDA4Mg,bWFjLW1pbmk,RmFsc2U,OTUyNQ,VHJ1ZQ,OTUzMg,OTUzMg,U3RhbmRhcmQ,,;A7eiXxLGFFM7lGGp+ZPmbntKx/ViM6i1JefDezLXqKzYp39Lc8p7GUe8nDSqv3mmq2TedSW5Fxk7WX3sQzfBgVnzt/pMKod1yTZ7StaS6qD7ytS/zpIrMxMjafnrtjVG4M7ZVpIiSzmLUAxOAMrG9R79ZXLi6ZalDK0PQQe9nOc";
        }
        Object parse = LicenseMange.parse(license, isTest, hack);
        log.warn("parse: {}", parse);
    }


    @Test
    public void verifyData() throws Exception {

    }
}
