import com.grapecity.documents.excel.internals.bJ.bO;
import com.grapecity.documents.excel.internals.bJ.bR;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

@Slf4j
class LicenseMangeTest {

    @Test
    public void getPubKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        RSAPublicKey publicKey = LicenseMange.getX509PublicKey("./src/main/resources/public.pem");
        log.debug("公钥模数 n (B10): {}", publicKey.getModulus());
        log.debug("公钥指数 e (B10): {}", publicKey.getPublicExponent());
        log.debug("公钥模数 n (B16): {}", publicKey.getModulus().toString(16));
        log.debug("公钥指数 e (B16): {}", publicKey.getPublicExponent().toString(16));
        log.debug("公钥模数 n (B64): {}", Base64.getEncoder().encodeToString(publicKey.getModulus().toByteArray()));
        log.debug("公钥指数 e (B64): {}", Base64.getEncoder().encodeToString(publicKey.getPublicExponent().toByteArray()));

        RSAPublicKey testPublicKey = LicenseMange.getX509PublicKey("./src/main/resources/test_public.pem");
        log.debug("公钥模数 n (B10): {}", testPublicKey.getModulus());
        log.debug("公钥指数 e (B10): {}", testPublicKey.getPublicExponent());
        log.debug("公钥模数 n (B16): {}", testPublicKey.getModulus().toString(16));
        log.debug("公钥指数 e (B16): {}", testPublicKey.getPublicExponent().toString(16));
        log.debug("公钥模数 n (B64): {}", Base64.getEncoder().encodeToString(testPublicKey.getModulus().toByteArray()));
        log.debug("公钥指数 e (B64): {}", Base64.getEncoder().encodeToString(testPublicKey.getPublicExponent().toByteArray()));
    }


    @Test
    void licenseParse() {
        String var0 = System.getenv("GCLMTEST");
        boolean result = bR.d(var0, "true", bO.d);
        log.debug("result: {}", result);

        String license = LicenseMange.GetLicense();
//        license = "NmJmNjMwZWEtMjJkMy00N2I1LWJiOWUtMjEwMmYzYzUyMTg2,NjA2NDExMDdYWFhYWFhYWDA4Mg,bWFjLW1pbmk,RmFsc2U," +
//                "OTUyNQ,VHJ1ZQ,OTUzMg,OTUzMg,U3RhbmRhcmQ,,;A7eiXxLGFFM7lGGp+ZPmbntKx/ViM6i1JefDezLXqKzYp39Lc8p7GUe8nDSqv3mmq2TedSW5Fxk7WX3sQzfBgVnzt/pMKod1yTZ7StaS6qD7ytS/zpIrMxMjafnrtjVG4M7ZVpIiSzmLUAxOAMrG9R79ZXLi6ZalDK0PQQe9nOc";
        Object parse = LicenseMange.parse(license);
        log.warn("parse: {}", parse);
    }


    @Test
    public void verifyData() throws Exception {

    }
}
