package org.codecrafterslab.agent.plugin;

import java.math.BigInteger;
import java.util.*;

public class ArgsFilter {
    private static final Map<String, BigInteger[]> store = new HashMap<>();
    private static final Set<String> logged = new HashSet<>();

    public static void addHexRule(String key, String modulus) {
        addRule(key, "10001", modulus, 16);
    }

    public static void addRule(String key, String modulus) {
        addRule(key, "65537", modulus, 10);
    }

    public static void addRule(String key, String exponent, String modulus) {
        addRule(key, exponent, modulus, 10);
    }

    public static void addRule(String key, String exponent, String modulus, int radix) {
        store.put(key, new BigInteger[]{new BigInteger(modulus, radix), new BigInteger(exponent, radix)});
    }

    public static BigInteger[] match(BigInteger modulus, BigInteger exponent) {
        String key = PairFinger.sha256Hex(modulus, exponent);
        boolean matched = store.containsKey(key);
        if (matched && logged.add(key)) {
            System.out.printf("store[%d] has key: %s%n", store.size(), key);
            System.out.printf("公钥模数 n (B10): %s%n", modulus);
            System.out.printf("公钥指数 e (B10): %s%n", exponent);
            System.out.printf("公钥模数 n (B16): %s%n", modulus.toString(16));
            System.out.printf("公钥指数 e (B16): %s%n", exponent.toString(16));
            System.out.printf("公钥模数 n (B64): %s%n", Base64.getEncoder().encodeToString(modulus.toByteArray()));
            System.out.printf("公钥指数 e (B64): %s%n", Base64.getEncoder().encodeToString(exponent.toByteArray()));
        }
        return store.getOrDefault(key, null);
    }

    static {
        addHexRule("f80713e1113a4ce28d03d9def62fb9c1dd390e581abb4f78e833802ca84775db", "974320bc887ede9de5c9eef2486fd3afd25384d826171fb34f7436f81c840c92bb417732f540d19c5e22c49eba7f7bf0225fc58f430655256f745e4331e54e2603e12c285ed435e705c47d1e7ab0a364d67adf999fa7c1e8fd1be9f86482862f01bd8a40a22ea6e089f615596d2d9b612ec6df3573d08398cc42ca049ece940e40da0feb345677e2a54fa3f94d0ec0546a9281faf5a8b42943ac63930facd149c71396b88d64229bd13a9391d5ff43f95fdc88bfebcbddd87dee149312969cc8ccea0f7a38ed8f66bd78e0fab1395d5ed98f4c50d9856c817d1aaabb228b048650bd8cbb8da9ee3d0206755a480b96360875bf7e0e1aa34c35da5d05f6464fc5");
        addHexRule("5b324484d966e9710044a13318a619adcba144f2b88c07ba3abb5bc276bb50f5", "aaf904f84b88229bdd17346545453294905185fc09569cd9a92a4be0b6e24f5e1e02e671351c996b2e1ab3db68013abb1904c3b2cf1496f812d174fd87561bb32c25954ecc2edf438e3e9edd8bd523b06a7e5914b5e5c9c8672e2e9558c741f1ddb44eb72bab8f3726d4e95a3f807f7add9c479cc628ca2ba3ca0d05572893d42a0df5a4751005e554a0b25a5290c9b490321d7a7c6d8d0e50c1a9f060a7a05f143ce9021cbb5bb31f15acc7b37cd1685b3a1a7770ef83ce5e6d40c9374aac9e6bfb57da7e06088e72e53355d2775e79eff4f56c8e6f5b9b9756bbffcd71dadd12b20c4a932a656c1a3b72a9a44a246278b9044bccebd8df639fff5d1080616d");

        // v9
        addHexRule("0502b84049bf2976dcfb422db9b4fdff8b85e68bccbe7e24dbc2ca1f89b3f39a", "974320bc887ede9de5c9eef2486fd3afd25384d826171fb34f7436f81c840c92bb417732f540d19c5e22c49eba7f7bf0225fc58f430655256f745e4331e54e2603e12c285ed435e705c47d1e7ab0a364d67adf999fa7c1e8fd1be9f86482862f01bd8a40a22ea6e089f615596d2d9b612ec6df3573d08398cc42ca049ece940e40da0feb345677e2a54fa3f94d0ec0546a9281faf5a8b42943ac63930facd149c71396b88d64229bd13a9391d5ff43f95fdc88bfebcbddd87dee149312969cc8ccea0f7a38ed8f66bd78e0fab1395d5ed98f4c50d9856c817d1aaabb228b048650bd8cbb8da9ee3d0206755a480b96360875bf7e0e1aa34c35da5d05f6464fc5");
    }
}
