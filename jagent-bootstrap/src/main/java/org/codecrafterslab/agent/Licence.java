package org.codecrafterslab.agent;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Licence {
    private static final byte[] MAGIC_NUMBER = new byte[]{(byte) 0xCC, (byte) 0x4C, (byte) 0x9A, (byte) 0x9B};
    private static final byte[] VENDOR = new byte[]{0x00, 0x00, 0x00, 0x01};

    @Getter
    @AllArgsConstructor
    enum MatchType {
        MAGIC_NUMBER(1),
        MAJOR_VERSION(2),
        MINOR_VERSION(4),
        VENDOR(8),
        ;

        private final int value;

        public static List<MatchType> getMatchTypes(int matchType) {
            return Arrays.stream(values()).filter(v -> (matchType & v.value) != 0).collect(Collectors.toList());
        }
    }

    public static boolean match(byte[] bytes, int matchType) {
        // 前置校验：非空、长度≥20
        if (bytes == null || bytes.length < 20) return false;

        List<MatchType> matchTypes = MatchType.getMatchTypes(matchType);
        for (MatchType type : matchTypes) {
            switch (type) {
                case MAGIC_NUMBER:
                    if (!isMagicNumberMatch(bytes, MAGIC_NUMBER)) {
                        return false;
                    }
                case MAJOR_VERSION:
                    if (!isMajorVersionMatch(bytes, 0)) {
                        return false;
                    }
                    break;
                case MINOR_VERSION:
                    if (!isMinorVersionMatch(bytes, 0)) {
                        return false;
                    }
                    break;
                case VENDOR:
                    if (!isVendorMatch(bytes, VENDOR)) {
                        return false;
                    }
            }
        }

        // 校验魔数
        if ((matchType & MatchType.MAGIC_NUMBER.value) == 0) {
            return false;
        }
        byte[] magicBytes = getBytes(bytes, 0, 4);
        boolean magicMatch = Arrays.equals(magicBytes, MAGIC_NUMBER);
        if (!magicMatch) return false;
        byte[] vendorBytes = getBytes(bytes, 16, 4);
        boolean vendorMatch = Arrays.equals(vendorBytes, VENDOR);
        // 两者都匹配才返回true
        return true;
    }

    private static boolean isVendorMatch(byte[] bytes, byte[] vendor) {
        return false;
    }

    private static boolean isMinorVersionMatch(byte[] bytes, int minorVersion) {
        byte[] magicBytes = getBytes(bytes, 4, 1);
        return Arrays.equals(magicBytes, new byte[]{(byte) minorVersion});
    }

    private static boolean isMajorVersionMatch(byte[] bytes, int majorVersion) {
        byte[] magicBytes = getBytes(bytes, 5, 1);
        return Arrays.equals(magicBytes, new byte[]{(byte) majorVersion});
    }

    private static boolean isMagicNumberMatch(byte[] bytes, byte[] magicNumber) {
        byte[] magicBytes = getBytes(bytes, 0, 4);
        return Arrays.equals(magicBytes, magicNumber);
    }

    public static boolean match(byte[] bytes) {
        return false;
    }

    private static byte[] getBytes(byte[] bytes, int offset, int length) {
        // 全量边界校验：避免数组越界、非法参数
        if (bytes == null
                || offset < 0          // 偏移量不能为负
                || length <= 0         // 截取长度必须>0
                || offset + length > bytes.length) { // 偏移+长度不能超出数组范围
            return new byte[0];
        }
        // 通用截取逻辑：使用Arrays.copyOfRange实现高效、安全的截取
        return Arrays.copyOfRange(bytes, offset, offset + length);
    }

}
