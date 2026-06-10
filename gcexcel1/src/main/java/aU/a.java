package aU;

public enum a {
    a, // NoLicense         0	无 license 文件
    b, // InvalidLicense	1	License 无效
    c, // ProductActivated	2	产品已激活 ✅
    d, // ProductExpired	3	产品已过期
    e, // TrialActivated	4	试用激活
    f; // TrialExpired	    5	试用过期

    public static final int g = 32;
    private static final a[] h = values();

    public int getValue() {
        return this.ordinal();
    }

    public static a forValue(int var0) {
        return h[var0];
    }
}
