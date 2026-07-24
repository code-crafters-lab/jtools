import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CodeUtils 单元测试")
class CodeUtilsTest {

    private static final Pattern ALPHANUMERIC = Pattern.compile("^[0-9A-Z]+$");

    @Nested
    @DisplayName("sha256ShortCode(String) - 默认长度")
    class DefaultLength {

        @Test
        @DisplayName("相同输入始终产生相同输出")
        void deterministic() {
            String hash1 = CodeUtils.sha256ShortCode("test123");
            String hash2 = CodeUtils.sha256ShortCode("test123");
            assertEquals(hash1, hash2);
        }

        @Test
        @DisplayName("不同输入产生不同输出")
        void differentInputs() {
            String hash1 = CodeUtils.sha256ShortCode("inputA");
            String hash2 = CodeUtils.sha256ShortCode("inputB");
            assertNotEquals(hash1, hash2);
        }

        @Test
        @DisplayName("输出仅包含 A-Z 和 0-9")
        void validChars() {
            String hash = CodeUtils.sha256ShortCode("你好123!@#");
            assertTrue(ALPHANUMERIC.matcher(hash).matches(),
                    "短码应仅包含大写字母和数字，实际: " + hash);
        }

        @Test
        @DisplayName("默认输出长度为 10 位")
        void length() {
            String hash = CodeUtils.sha256ShortCode("any");
            assertEquals(10, hash.length());
        }

        @Test
        @DisplayName("空字符串可正常生成短码")
        void emptyString() {
            String hash = CodeUtils.sha256ShortCode("");
            assertNotNull(hash);
            assertEquals(10, hash.length());
            assertTrue(ALPHANUMERIC.matcher(hash).matches());
        }

        @Test
        @DisplayName("1000 个不同输入产生 1000 个唯一短码")
        void uniqueness() {
            int n = 1000;
            Set<String> set = new HashSet<>();
            for (int i = 0; i < n; i++) {
                set.add(CodeUtils.sha256ShortCode("key-" + i));
            }
            assertEquals(n, set.size());
        }

        @Test
        @DisplayName("输入为 null 时抛出 NullPointerException")
        void nullInput() {
            assertThrows(NullPointerException.class, () -> CodeUtils.sha256ShortCode((String) null));
        }

        @Test
        @DisplayName("长字符串输入可正常生成短码")
        void longInput() {
            String longInput = String.join("", Collections.nCopies(10_000, "a"));
            String hash = CodeUtils.sha256ShortCode(longInput);
            assertNotNull(hash);
            assertEquals(10, hash.length());
            assertTrue(ALPHANUMERIC.matcher(hash).matches());
        }

        @Test
        @DisplayName("Unicode 表情符号可正常生成短码")
        void unicodeEmoji() {
            String hash = CodeUtils.sha256ShortCode("\uD83D\uDE00\uD83D\uDE01");
            assertNotNull(hash);
            assertEquals(10, hash.length());
            assertTrue(ALPHANUMERIC.matcher(hash).matches());
        }
    }

    @Nested
    @DisplayName("sha256ShortCode(String, int) - 自定义长度")
    class CustomLength {

        @Test
        @DisplayName("指定长度为 6 时输出 6 位短码")
        void customLength6() {
            String hash = CodeUtils.sha256ShortCode("data", 6);
            assertEquals(6, hash.length());
            assertTrue(ALPHANUMERIC.matcher(hash).matches());
        }

        @Test
        @DisplayName("指定长度为 1 时输出 1 位短码")
        void customLength1() {
            String hash = CodeUtils.sha256ShortCode("data", 1);
            assertEquals(1, hash.length());
            assertTrue(ALPHANUMERIC.matcher(hash).matches());
        }

        @Test
        @DisplayName("指定长度为 32 时输出 32 位短码（等于摘要长度）")
        void customLength32() {
            String hash = CodeUtils.sha256ShortCode("data", 32);
            assertEquals(32, hash.length());
            assertTrue(ALPHANUMERIC.matcher(hash).matches());
        }

        @Test
        @DisplayName("相同输入相同长度，结果一致")
        void deterministic() {
            String hash1 = CodeUtils.sha256ShortCode("test", 8);
            String hash2 = CodeUtils.sha256ShortCode("test", 8);
            assertEquals(hash1, hash2);
        }

        @Test
        @DisplayName("不同长度产生不同输出")
        void differentLengths() {
            String hash8 = CodeUtils.sha256ShortCode("data", 8);
            String hash10 = CodeUtils.sha256ShortCode("data", 10);
            assertNotEquals(hash8, hash10);
        }
    }

    @Nested
    @DisplayName("digestToShortCode - 参数校验")
    class ParameterValidation {

        @Test
        @DisplayName("长度为 0 时抛出 IllegalArgumentException")
        void lenZero() {
            assertThrows(IllegalArgumentException.class,
                    () -> CodeUtils.sha256ShortCode("test", 0));
        }

        @Test
        @DisplayName("长度为负数时抛出 IllegalArgumentException")
        void lenNegative() {
            assertThrows(IllegalArgumentException.class,
                    () -> CodeUtils.sha256ShortCode("test", -1));
        }

        @Test
        @DisplayName("长度超过摘要长度时抛出 IllegalArgumentException")
        void lenExceedsDigest() {
            // SHA-256 摘要为 32 字节，传入 33 应抛异常
            assertThrows(IllegalArgumentException.class,
                    () -> CodeUtils.sha256ShortCode("test", 33));
        }
    }

    @Nested
    @DisplayName("sha256ShortCode(byte[]) - 字节数组输入")
    class ByteArrayDefaultLength {

        @Test
        @DisplayName("相同字节数组始终产生相同输出")
        void deterministic() {
            byte[] data = {1, 2, 3, 4, 5};
            String hash1 = CodeUtils.sha256ShortCode(data);
            String hash2 = CodeUtils.sha256ShortCode(data);
            assertEquals(hash1, hash2);
        }

        @Test
        @DisplayName("不同字节数组产生不同输出")
        void differentInputs() {
            String hash1 = CodeUtils.sha256ShortCode(new byte[]{1, 2, 3});
            String hash2 = CodeUtils.sha256ShortCode(new byte[]{4, 5, 6});
            assertNotEquals(hash1, hash2);
        }

        @Test
        @DisplayName("输出仅包含 A-Z 和 0-9，长度为 10")
        void validCharsAndLength() {
            String hash = CodeUtils.sha256ShortCode(new byte[]{0, -1, 127, -128});
            assertEquals(10, hash.length());
            assertTrue(ALPHANUMERIC.matcher(hash).matches());
        }

        @Test
        @DisplayName("空字节数组可正常生成短码")
        void emptyArray() {
            String hash = CodeUtils.sha256ShortCode(new byte[0]);
            assertNotNull(hash);
            assertEquals(10, hash.length());
            assertTrue(ALPHANUMERIC.matcher(hash).matches());
        }

        @Test
        @DisplayName("输入为 null 时抛出 NullPointerException")
        void nullInput() {
            assertThrows(NullPointerException.class, () -> CodeUtils.sha256ShortCode((byte[]) null));
        }
    }

    @Nested
    @DisplayName("sha256ShortCode(byte[], int) - 字节数组自定义长度")
    class ByteArrayCustomLength {

        @Test
        @DisplayName("指定长度为 8 时输出 8 位短码")
        void customLength8() {
            byte[] data = {10, 20, 30};
            String hash = CodeUtils.sha256ShortCode(data, 8);
            assertEquals(8, hash.length());
            assertTrue(ALPHANUMERIC.matcher(hash).matches());
        }

        @Test
        @DisplayName("相同字节数组相同长度，结果一致")
        void deterministic() {
            byte[] data = {1, 2, 3};
            String hash1 = CodeUtils.sha256ShortCode(data, 6);
            String hash2 = CodeUtils.sha256ShortCode(data, 6);
            assertEquals(hash1, hash2);
        }

        @Test
        @DisplayName("长度超过摘要长度时抛出 IllegalArgumentException")
        void lenExceedsDigest() {
            assertThrows(IllegalArgumentException.class,
                    () -> CodeUtils.sha256ShortCode(new byte[]{1}, 33));
        }
    }

    @Nested
    @DisplayName("String 与 byte[] 结果一致性")
    class Consistency {

        @Test
        @DisplayName("字符串与其 UTF-8 字节表示产生相同短码")
        void stringAndBytesMatch() {
            String input = "hello";
            String fromString = CodeUtils.sha256ShortCode(input);
            String fromBytes = CodeUtils.sha256ShortCode(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            assertEquals(fromString, fromBytes);
        }
    }
}
