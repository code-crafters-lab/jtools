import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Collectors;

public class KeyUtils {

    private static final String RSA = "RSA";

    /**
     * 读取 PEM 公钥文件，转换为 RSAPublicKey 对象
     *
     * @param pemPath PEM 公钥文件路径
     * @return RSA 公钥对象
     */
    public static RSAPublicKey getX509PublicKey(String pemPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] der = readPemDer(pemPath, "PUBLIC KEY");
        X509EncodedKeySpec spec = new X509EncodedKeySpec(der);
        return (RSAPublicKey) KeyFactory.getInstance(RSA).generatePublic(spec);
    }

    /**
     * 读取 PEM 私钥文件，转换为 RSAPrivateKey 对象
     *
     * @param pemPath PEM 私钥文件路径
     * @return RSA 私钥对象
     */
    public static RSAPrivateKey getX509PrivateKey(String pemPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] der = readPemDer(pemPath, "PRIVATE KEY");
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
        return (RSAPrivateKey) KeyFactory.getInstance(RSA).generatePrivate(spec);
    }

    /**
     * 读取 PEM 文件，提取 Base64 编码的 DER 数据并解码
     *
     * @param pemPath  PEM 文件路径
     * @param type  PEM 类型标识（如 "PUBLIC KEY"、"PRIVATE KEY"）
     * @return 解码后的 DER 字节数组
     */
    private static byte[] readPemDer(String pemPath, String type) throws IOException {
        String begin = "-----BEGIN " + type + "-----";
        String end = "-----END " + type + "-----";

        String pem = Files.readAllLines(Paths.get(pemPath))
                .stream()
                .filter(line -> !line.startsWith(begin) && !line.startsWith(end) && !line.isEmpty())
                .collect(Collectors.joining());

        return Base64.getDecoder().decode(pem);
    }

}
