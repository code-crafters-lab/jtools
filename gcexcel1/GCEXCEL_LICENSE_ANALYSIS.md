# GcExcel 9.0.1 License 授权体系分析

> 分析基于 `com.grapecitysoft.documents:gcexcel:9.0.1` + `jtools` 项目逆向工程

---

## 1. 项目架构总览

```
┌─────────────────────────────────────────────────────────────────┐
│                        GCDemo (入口)                             │
│  Workbook wb = new Workbook();  ← 触发 license 校验              │
│  wb.addDataSource("ds", new JsonDS(..., gson));                 │
│  wb.processTemplate();                                          │
│  wb.save("demo.xlsx");                                          │
└──────────────────────────┬──────────────────────────────────────┘
                           │
          ┌────────────────▼───────────────────────────────────┐
          │              License 校验链 (双路径)                  │
          │  V9: aU.e → aV.k/aV.a → aW.c → aW.d RSA verify    │
          │  旧: aU.e → aX.b → aX.c → aX.a RSA verify          │
          └────────────────┬───────────────────────────────────┘
                           │
              ┌────────────┴────────────┐
              ▼                         ▼
┌─────────────────────────┐  ┌──────────────────────────┐
│  ConstSubstitutionPlugin │  │  PublicKeyPlugin          │
│  (常量池替换 aX.a 公钥)   │  │  (注入 RSAPublicKeySpec) │
│  ASM Visitor & LDC 指令  │  │  ASM Tree & ArgsFilter   │
│  ⚠ 未覆盖 V9 aV.k 公钥  │  │  V9 规则已添加 ✅         │
└─────────────────────────┘  └──────────────────────────┘
              │                         │
              ▼                         ▼
┌─────────────────────────┐  ┌──────────────────────────┐
│  jagent (Agent 框架)    │  │  jagent-bootstrap         │
│  ASM 字节码操纵 + SPI   │  │  ArgsFilter (Bootstrap)  │
└─────────────────────────┘  └──────────────────────────┘
```

### 模块依赖关系

```
jtools (root)
├── gcexcel/              ← Demo 入口，依赖 GcExcel 9.0.1
├── jagent/               ← Java Agent 核心框架 (ASM 字节码操纵)
├── jagent-bootstrap/     ← Bootstrap ClassLoader 加载 (ArgsFilter)
├── plugin-cs/            ← 常量替换 + 公钥替换插件
├── plugin-timing/        ← 方法耗时统计插件 (未实现桩代码)
└── distribution/         ← 聚合打包所有产出
```

---

## 2. License 文件格式

### 2.1 文件位置

```
~/.local/share/GrapeCity/{productUUID}/.license
~/.local/share/GrapeCity/{vendorUUID}/.license
```

两个 UUID：
| UUID | 用途 | 默认值 | 测试模式 (GCLMTEST=true) |
|------|------|--------|-------------------------|
| 产品 UUID | 优先尝试 | `6bf630ea-22d3-47b5-bb9e-2102f3c52186` | `e9b2a94e-afa0-40ab-be43-b86d3719c5f7` |
| 厂商 UUID | 回退选项 | `383d4eff-9ef1-4198-ad4d-eb11035a7bc6` | `64c28c83-fab2-4c5d-bd94-7e2dee4da186` |

### 2.2 文件结构

```
base64Data;base64Signature
```

- **数据段**: Base64 编码，逗号分隔 11 个字段
- **签名段**: Base64 编码，对数据段原文的 RSA/SHA256 签名

### 2.3 字段定义（索引 0-10）

| 索引 | 字段 | LicenseMange 映射 | aX.c$a 映射 | 类型 | 含义 |
|------|------|--------------------|-------------|------|------|
| 0 | UUID | `uuid` | `b` | UUID | 产品标识，决定 license 文件路径 |
| 1 | 序列号 | `serialNumber` | `c` | String | 激活序列号，`X` 为掩码占位 |
| 2 | 主机名 | `hostname` | `d` | String | 授权绑定的机器名 |
| 3 | 开发版 | `a` | `a` | boolean | true=开发版, false=部署版 |
| 4 | 激活时间 | `activeTime` | `e` | int | GcExcel 内部时间戳 |
| 5 | 永久有效 | `f` | `f` | boolean | true=永久, false=有期限 |
| 6 | 过期时间 | `expiryTime` | `g` | int | GcExcel 内部时间戳 |
| 7 | _(index 7)_ | **跳过未用** | — | — | 与 index 6 相同，冗余字段 |
| 8 | 版本 | `version` | `h` | String | Standard / Enterprise 等 |
| 9 | 保留 | `i` | `i` | String | 未使用 |
| 10 | 保留 | `j` | `j` | String | 未使用 |

### 2.4 内部时间戳说明

字段 4, 6, 7 的值（如 9525, 9532）不是标准 Unix 秒级时间戳，而是 **GcExcel 自定义基准**的偏移量。以 `days since 2000-01-01` 计算：

```
9525 days ≈ 26.08 years → 约 2026 年 (2000 + 26)
9532 days ≈ 26.10 years → 约 2026 年
```

---

## 3. 授权对象结构

### 3.1 `aX.c` — 核心授权对象（GcExcel 内部）

从 bytecode 反编译得到的 `aX.c` 是 GcExcel 内部的 license 解析结果对象：

```java
// com.grapecity.documents.excel.internals.aX.c
public class aX.c {
    private boolean a;          // 来源标记: true=SetLicenseKey, false=文件
    private String b;           // 原始 license 字符串

    public aU.a a();            // 授权状态结果枚举
    public boolean b();         // 是否通过校验 (signature + 字段解析)
    public String c();          // 解码后的 UUID
    public boolean d();
    public String e();          // 解码后的序列号
    public String f();          // 解码后的主机名
    public String g();          // 解码后的 flag (开发版)
    public String h();          // 解码后的激活时间
    public String i();          // 解码后的永久标志
    public String j();          // 解码后的过期时间
    public String k();          // 解码后的版本
    public aX.f l();            // UUID 对象
    public int m();             // 时间戳 int 值
    public boolean n();
}
```

### 3.2 `aX.c$a` — 内部解析组件

```java
// com.grapecity.documents.excel.internals.aX.c$a
// 实际存储解析后的 10 个字段
static class aX.c$a {
    boolean a;       // valid         是否有效
    UUID b;          // uuid          产品 UUID
    String c;        // serial        序列号
    String d;        // hostname      主机名
    int e;           // activeTime    激活时间
    boolean f;       // ??            是否永久
    int g;           // expiryTime    过期时间
    String h;        // version       版本
    String i;        // ??            保留
    String j;        // ??            保留
}
```

### 3.3 `LicenseMange.License` — 项目自定义映射

```java
// 项目 LicenseMange.java 内部类
private static class License {
    private boolean a;           // 开发版标记
    private UUID uuid;           // UUID
    private String serialNumber; // 序列号
    private String hostname;     // 主机名
    private int activeTime;      // 激活时间
    private boolean f;           // 永久有效
    private int expiryTime;      // 过期时间
    private String version;      // 版本 (Standard/Enterprise)
    private String i;            // 保留
    private String j;            // 保留
}
```

---

## 4. V9 授权系统

### 4.1 V9 系统总览

GcExcel 9.0.1 包含**两套独立的授权系统**：
- **旧版 (V1/V2)**: `aX.*` 包，分号分隔的 base64 格式，RSA 签名验证
- **V9**: `aV.*` + `aW.*` 包，JSON 格式 + 4 层编码混淆，独立 RSA 签名验证

两套系统在 `aU.e` 入口处融合：无论从哪套系统验证成功，最终状态都同步到 `aV.k` 管理。

### 4.2 `aV.k` — V9 授权状态管理器

`aV.k` 是 V9 系统的核心状态持有者，实现了 `aV.c` 接口：

```java
class aV.k implements aV.c {
    String b;                        // 当前 license 原始字符串
    aV.a c;                          // 激活控制器 (管理激活流程)
    aV.b d;                          // 当前激活提供者 (null = 未激活)
    bJ.v e;                          // 过期日期
    Boolean f;                       // 永久标记缓存
    boolean g;                       // 已报告标记 (防止重复报错)
    Event<aW.b<EventArgs>> a;        // License 变更事件
    Map<Character, aW.a> h;          // 解码器映射 (Character → Crypto)
    Map<Character, aW.d> i;          // 签名验证器映射 (Character → Signature)
    Map<Boolean, Func0Param<aV.b>> j;// 激活提供者工厂
}
```

**关键方法**:

| 方法 | 返回 | 说明 |
|------|------|------|
| `c()` | String | 获取当前 license key |
| `a()` | String | **返回 `"93W7"`** (V9 产品代码) |
| `b()` | String | 返回 `"localhost"` (开发主机名) |
| `e()` | aV.h | 通过 `d.c()` 获取授权状态；无激活时返回 `Unlicensed` |
| `f()` | boolean | 检查是否永久有效 |
| `g()` | boolean | 检查已报告标记 |
| `a(String)` | void | 设置 license key 并触发 `LicenseChanged` 事件 |

### 4.3 `aV.c` — V9 产品接口

```java
interface aV.c {
    String a();                    // 产品代码 (如 "93W7")
    String b();                    // 产品名称
    String c();                    // 当前 license key
    void a(String key);            // 设置 license key
    Event getLicenseChangedEvent(); // license 变更事件
}
```

### 4.4 `aW.a` — V9 Crypto 编码/解码器

```java
class aW.a {
    String a;                      // key1
    String b;                      // key2
    aW.a(String key1, String key2); // 构造
    String a();                    // get key1
    String b();                    // get key2
    String a(String encode);       // 用密钥对 data 编码
    String b(String decode);       // 用密钥对 data 解码
}
```

默认注册实例: `new aW.a("Sample", "A1")` — 这看似占位符，实际 crypto 逻辑在 `aW.a` 内部实现。

### 4.5 `aW.d` — V9 Signature 签名验证器

```java
class aW.d {
    Signature a;                   // java.security.Signature 实例 (SHA256withRSA)
    int b;                         // 期望密钥位数: 2048
    String c;                      // RSA 模数 (Base64 编码)
    String d;                      // RSA 指数 (Base64 编码)
    aW.d(String modulusB64, String exponentB64); // 从 Base64 构建 RSA 公钥
    boolean a(String data, String signatureB64); // 验证 RSA/SHA256 签名
}
```

**构造过程**:
1. `Signature.getInstance("SHA256withRSA")` — 初始化签名算法
2. 设置期望密钥位数 `b = 2048`
3. 存储模数字符串 `c` 和指数 `d`

**验证过程** `aW.d.a(data, signatureB64)`:
1. 如果签名串 `signatureB64` 为空 → 返回 `false`
2. 解码模数: `BigInteger(1, Base64.decode(modulus.getBytes(UTF_8)))` — **验证模数长度必须为 2048 位 (256 字节)**
3. 解码指数: `BigInteger(1, Base64.decode(exponent.getBytes(UTF_8)))`
4. 构造 `RSAPublicKeySpec(modulusBI, exponentBI)` → `KeyFactory("RSA")` → `RSAPublicKey`
5. `Signature.initVerify(publicKey)` — 初始化为验证模式
6. `Signature.update(data.getBytes(UTF_8))` — 输入被签名的数据
7. `Signature.verify(Base64.decode(signatureB64.getBytes()))` — 验证签名

**V9 独立 RSA 公钥** (硬编码在 `aV.k.a(Character)` 中):

```
模数:   udkL9Yp1FotxuamEH+Q3l7d6r+PAhQ6YypLjvRCINZ19FKWe4DiN4VCRkxrP4kPaubPzoi0WNHThfTzrc8PToRuzQmbCxsvb1IsS6rOssOjFVvT09InQw5jogiY9nnVnA0lgmHd43KY1jvkiLV/iK+h4n0tz9w9D812eL6RVOYKzeN+uXsdOxrS01D+liBPx3UGfcNYW/jY5me7OQQPB5I16zvmifWqr68EVuRchI5xc+nDzu6lZghrMl33Lsbx8JljQiXmUi2PGSdXmj6adqPjeyLdHlPWuFMUEljYgVgaxAKxGBNyPm08lVMFnTpJUpQLB9Inc51itnFrob9wpEQ==
指数:   AQAB (65537)
```

> ⚠ 注意: 此 V9 RSA 公钥**不同于**旧版 `aX.a` 中的两个公钥 (生产和测试)。已通过 `ArgsFilter` 第 3 条规则覆盖。

### 4.6 `aV.a` — 激活控制器

`aV.a` 是 V9 的激活控制器，接收 `aV.k` 传入的 decoder/verifier 映射，管理完整的激活流程：

```java
class aV.a {
    static ConcurrentHashMap<String, aV.e> cache;  // 全局激活缓存 (key=产品代码)
    aV.c product;                                   // 产品接口 (aV.k 实例)
    Map<Character, aW.a> decoders;                  // 解码器映射 (aV.k.h → `aW.a("Sample","A1")`)
    Map<Character, aW.d> verifiers;                 // 验证器映射 (aV.k.i → `aW.d(V9_key, AQAB)`)
    Map<Boolean, Func0Param<aV.b>> providers;       // 激活提供者工厂 (aV.k.j)
    aW.b<EventArgs> listener;                       // License 变更监听器
    boolean i;                                      // "#A0" 模式标记 (true=绕过 RSA 验证)
    static Locale locale;                           // empty locale for case-insensitive matching
}
```

#### 激活流程: `aV.a.a()` (无参, 主入口)

```
1. 检查缓存: cache.containsKey("93W7")
   ├─ 缓存命中 → c() 方法从缓存中返回 aV.e
   │    └─ 遍历 aV_e.f.j (Prd 数组), 找到第一个 C=="93W7" 的条目
   │         └─ 要求: aV_e.e (签名) 非空, aV_e.f (license 数据) 非空, 且有 Prd 数组
   └─ 缓存未命中 → 执行完整激活流程 (步骤 2)
```

```
2. 完整激活流程:
   a. 获取 license key: product.c()       → aV.k.c() → 返回存储的 key 字符串
   b. 获取解码器: decoders.get('c')       → aV.k.h['c'] → aW.a("Sample", "A1")
   c. 获取分隔符: decoder.b()              → aW.a.b() → 返回 "#A1"
   d. 调用 a(key, "#A1") 解析 key → aV.e  (详见下文「key 解析」)
   e. 获取验证器: verifiers.get('s')       → aV.k.i['s'] → aW.d(V9_modulus, AQAB)
   f. 检查 A0 模式:
      ─ 如果 i==true (解析时使用了 "#A0" 分隔符): 跳过 RSA 验证 ✓
      ─ 否则:
        数据 = String.format(Locale.ROOT, "%s%s%s",
                  aV_e.b,     // 前缀字符 (如 "c")
                  aV_e.a,     // 分隔符 (如 "#A1")
                  aW.c.a(aV_e.f))  // aV.f 序列化为 JSON
        签名 = aV_e.e        // 从 JSON "S" 字段提取
        验证 = verifier.a(data, signature)
         ─ 验证成功: 继续
         ─ 验证失败: 抛出 aV.g("Verify signature failed, Invalid License")
   g. 回调 onLicenseReady:
      supplier = lambda → a(aV_e)  // 将 aV.e 转为 aV.b 并提供者
      布尔标记 = !i && a(aV_e.f)   // 检查 license 有效性
      providers.invoke(布尔标记, supplier)  → aV.k.a(bool, Supplier)
```

#### key 解析: `aV.a.a(String key, String separator)`

该方法从 key 字符串中提取前缀、解码混淆数据、解析 JSON:

```
1. key 为空 → 抛出异常
2. 将 key 和 separator 转为小写进行匹配
3. 在 key 中查找 separator ("#a1")
   ─ 如果没找到: 尝试 "#a0"
        ├─ 如果 "#a0" 也没找到: 抛出异常
        └─ 如果找到 "#a0": 设置 i=true (标记为 A0 模式, 使用 "#A0" 作为分隔符)
4. 提取前缀: prefix = key[0..分隔符位置)
5. 提取混淆数据: data = key[分隔符位置 + 分隔符长度..末尾]
6. 创建临时解码器: aW.a("c", "A1")
7. 解码: decoder.b(data) → aW.e.b(data) → 明文 JSON 字符串
8. Gson 反序列化: aW.c.a(jsonString) → aV.e (含 "S" 和 "D" 字段)
9. 设置字段:
   aV_e.a = 实际分隔符 ("#A1" 或 "#A0")  — 作为 crypto key
   aV_e.b = 前缀字符
10. 返回 aV_e
```

### 4.7 `aV.e` — 激活缓存条目

```java
class aV.e {
    String a;           // 分隔符 (如 "#A1" 或 "#A0") — 作为 crypto key 标识
    String b;           // 前缀字符 (如 "c" — key 的第一个字符)
    String[] c;         // 域名绑定 (Dms 字段 split)
    int d;              // 未知 (内部计数/状态?)
    String e;           // "S" 字段 — RSA 签名 (Base64 编码)
    aV.f f;             // "D" 字段 — 结构化 license 信息模型
}
```

### 4.8 `aV.f` — V9 结构化 License 信息模型

```java
class aV.f {
    String a;           // Id (License ID)
    boolean b;          // Evl (Evaluation 标记)
    String c;           // OId (Organization ID)
    String d;           // CNa (Company Name)
    String e;           // CId (Contact ID)
    String f;           // Dms (Domains 绑定)
    String g;           // Ips (IP 地址绑定)
    Date h;             // Exp (过期时间)
    Date i;             // Crt (创建时间)
    aV.i[] j;           // Prd (产品数组)
    Boolean k;          // Anl.dsr (反序列化标记)
    String l;           // Anl.ver (版本)
}
```

### 4.9 `aV.i` — 产品信息

```java
class aV.i {
    String a;           // N (Product Name, 如 "GcExcel")
    String b;           // C (Product Code, 如 "GCEXCEL")
}
```

### 4.10 `aW.c` — V9 JSON 序列化/反序列化

使用 **Gson** 库进行 JSON 处理：

- `aW.c.a(aV.f)` → **序列化**: `aV.f` 模型 → JSON 字符串
- `aW.c.a(String)` → **反序列化**: JSON 字符串 → `aV.e` 对象

**V9 JSON 格式**:
```json
{
  "Id": "license-id",
  "Evl": false,
  "OId": "org-id",
  "CNa": "company-name",
  "CId": "contact-id",
  "Dms": "*.example.com",
  "Ips": "192.168.1.*",
  "Exp": "2026-06-01",
  "Crt": "2026-01-01",
  "Prd": [
    { "N": "GcExcel", "C": "GCEXCEL" }
  ],
  "Anl": {
    "dsr": true,
    "ver": "v2"
  }
}
```

### 4.11 `aW.e` — V9 编码混淆层

V9 license 字符串不是裸 JSON/Base64，而是经过 4 层混淆编码，所有逻辑集中在 `aW.e` 类中：

**编码流水线** `aW.e.a(plaintext)`:
```
plaintext
  → c(s): Base64.encode(s.getBytes(UTF_8))              // 标准 Base64
  → replace("==", "&")                                   // 替换 padding
  → replace("=", "#")                                    // 替换剩余等号
  → a(s, true): 向右旋转, 分割点 = floor(len/2)          // 旋转
  → e(s): new StringBuilder(s).reverse()                 // 反转
  → f(s): 正向交换字符对 + 大小写翻转 + 数字变换         // 字符变换
  → final encoded string
```

**解码流水线** `aW.e.b(encoded)`:
```
encoded string
  → g(s): 逆向交换字符对 + 大小写翻转 + 数字变换         // 字符变换 (反向)
  → e(s): new StringBuilder(s).reverse()                 // 反转
  → a(s, false): 向左旋转, 分割点 = ceil(len/2)          // 旋转 (逆向)
  → replace("#", "=")                                    // 还原等号
  → replace("&", "==")                                   // 还原 padding
  → d(s): Base64.decode(s.getBytes(UTF_8)) → String       // 标准 Base64 解码
  → plaintext
```

**各层算法详解**:

| 层 | 方法 | 操作细节 |
|----|------|----------|
| Base64 编码 | `c(s)` | `java.util.Base64.getEncoder().encodeToString(s.getBytes(UTF_8))` |
| Base64 解码 | `d(s)` | `new String(Base64.getDecoder().decode(s.getBytes(UTF_8)), UTF_8)` |
| 符号替换 (编码) | `replace()` | `==` → `&`, `=` → `#` (按顺序, 先长后短) |
| 符号替换 (解码) | `replace()` | `#` → `=`, `&` → `==` (按顺序, 先短后长) |
| 旋转 (编码) | `a(s, true)` | 分割点 = `s.length()/2`; 结果 = `s.substring(split) + s.substring(0, split)` **(右半部分移到前面)** |
| 旋转 (解码) | `a(s, false)` | 偶数长度: 分割点 = `s.length()/2`; 奇数长度: 分割点 = `s.length()/2 + 1`; 结果 = `s.substring(split) + s.substring(0, split)` |
| 反转 | `e(s)` | `new StringBuilder(s).reverse().toString()` |

**字符交换与变换** `f(s)` (编码) / `g(s)` (解码):

```
f(s): 正向遍历 (i=0; i < len-4; i++)
  → a(arr, i,   i+2,  dir=1)    // 交换 arr[i] ↔ arr[i+2], 同时变换字符
  → a(arr, i+1, i+3,  dir=1)    // 交换 arr[i+1] ↔ arr[i+3], 同时变换字符

g(s): 逆向遍历 (i=len-5; i >= 0; i--)
  → a(arr, i,   i+2,  dir=-1)   // 反向交换 + 反向变换
  → a(arr, i+1, i+3,  dir=-1)   // 反向交换 + 反向变换
```

**`a(char[], i, j, dir)` 交换函数**: `temp = arr[i]; arr[i] = transform(arr[j], dir); arr[j] = transform(temp, dir)`

**`a(Character, dir)` 变换函数** `a(char c, int dir)`:

| 字符类型 | 变换规则 |
|----------|----------|
| 大写字母 `A-Z` | → 小写 `a-z` (dir 无关) |
| 小写字母 `a-z` | → 大写 `A-Z` (dir 无关) |
| 数字 `0-9` | `((c - '0') + 10 + dir) % 10 + '0'` → 编码时 +1, 解码时 -1 |
| 其他字符 | 不变 |

### 4.12 两套授权系统对比总结

| 维度 | 旧版 (V1/V2) | V9 |
|------|-------------|-----|
| **包路径** | `aX.*` (obfuscated) | `aV.*`, `aW.*` (obfuscated) |
| **格式** | `base64Data;base64Signature` | Obfuscated JSON (4 层混淆) |
| **解析器** | `aX.c` 逗号分割 11 字段 | `aW.c` Gson JSON → `aV.e` |
| **RSA 验签** | `aX.a` (2 个公钥) | `aW.d` + `aV.k` (1 个独立公钥) |
| **编码混淆** | 无 (纯 base64) | `aW.e` 4 层混淆 |
| **入口** | `aX.b` env/file 加载 | `aV.k` 直接 key 设置 |
| **状态管理** | `aX.c` + `aX.d`/`aX.e` | `aV.k` + `aV.a`/`aV.e` 缓存 |
| **前缀系统** | 无 | 按字符前缀选择 decoder+verifier |
| **#A0 旁路** | 无 | `#A0` 分隔符可绕过 RSA 验证 |
| **产品代码** | UUID (aX.f) | `"93W7"` (aV.k.a()) |
| **事件** | 无 | LicenseChanged 事件 |
| **是否被覆盖** | ✅ 两层插件已覆盖 | ✅ ArgsFilter 第 3 条规则已覆盖 |

---

## 5. 授权状态枚举体系

### 5.1 `aU.a` — License 验证结果（6 值枚举）

| 常量 | 解码值 | ordinal | 含义 |
|------|--------|---------|------|
| `a` | `NoLicense` | 0 | 无 license 文件 |
| `b` | `InvalidLicense` | 1 | License 无效 |
| `c` | `ProductActivated` | 2 | **产品已激活** ✅ |
| `d` | `ProductExpired` | 3 | 产品已过期 |
| `e` | `TrialActivated` | 4 | 试用激活 |
| `f` | `TrialExpired` | 5 | 试用过期 |

### 5.2 `aV.h` — 授权状态（3 值枚举，带 int 值）

| 常量 | 名称 | int 值 | 含义 |
|------|------|--------|------|
| `a` | `Evaluation` | -1 | 评估模式 |
| `b` | `Unlicensed` | 0 | 未授权 |
| `c` | `Licensed` | 1 | **已授权** ✅ |

### 5.3 `aU.f` — License 类型（3 值枚举）

| 常量 | 解码值 | 含义 |
|------|--------|------|
| `a` | `DevV2` | 开发版 V2 |
| `b` | `DeployV2` | 部署版 V2 |
| `c` | `ProductV1` | 产品版 V1 |

### 5.4 `aZ.m` — 功能模块（4 值枚举）

| 常量 | 解码值 | 含义 |
|------|--------|------|
| `a` | `Excel` | Excel 核心功能 |
| `b` | `PdfAndPrint` | PDF/打印功能 |
| `c` | `Image` | 图片处理 |
| `d` | `Html` | HTML 导出 |

### 5.5 `aX.e` — License 解析结果（4 值枚举）

| 常量 | 解码值 | 含义 |
|------|--------|------|
| `a` | `Success` | 解析成功 |
| `b` | `ParseFailure` | 解析失败 |
| `c` | `MachineNameMismatch` | 主机名不匹配 |
| `d` | `OtherProduct` | 其他产品 |

### 5.6 状态转换关系

```
aX.e (解析结果)           aU.a (验证结果)          aV.h (授权状态)
───────────────          ──────────────          ──────────────
Success  ───────────────→ ProductActivated ────→ Licensed
Success + expired ──────→ ProductExpired
Success + dev ──────────→ TrialActivated
Success + dev + expired → TrialExpired
ParseFailure ───────────→ InvalidLicense ──────→ Evaluation
(no file) ──────────────→ NoLicense ───────────→ Unlicensed
MachineNameMismatch ────→ InvalidLicense
OtherProduct ───────────→ InvalidLicense
```

---

## 6. License 验证完整流程

### 6.1 调用链

```
Workbook.<init>()
  └─ aU.e.a()                          ← 授权评估入口
       ├─ aV.k.e()                     ← 检查当前状态
       │   ├─ (Licensed) → 返回 aU.b 包装的激活状态
       │   └─ (Unlicensed) → 尝试加载 license
       │
       ├─ V9 路径 (key 不含 ";") ←────
       │   aV.k.a(key)                ← 直接设置到 V9 管理器
       │   └─ 触发 LicenseChanged 事件
       │
       ├─ 旧版路径 (key 含 ";") ←─────
       │   aX.b.a(key/file)           ← 加载/存储原始字符串
       │   ├─ SetLicenseKey() 直接设置
       │   ├─ 环境变量 GCEXCEL_JAVA_DEPLOY_LICENSE_V9
       │   ├─ SetLicenseFile() 文件路径
       │   └─ 默认路径: ~/.local/share/GrapeCity/{uuid}/.license
       │
       ├─ aX.c.parse(licenseString)    ← 旧版解析
       │   ├─ split(";") → [data, signature]
       │   ├─ aX.a.a(data, signature)  ← RSA/SHA256 签名验证
       │   │   ├─ 获取公钥 (硬编码模数 + 指数 65537)
       │   │   ├─ 选择: 生产公钥 / 测试公钥 (GCLMTEST=true)
       │   │   └─ Signature.getInstance("SHA256WithRSA").verify()
       │   └─ 解析 11 个逗号分隔字段
       │
       └─ 状态同步: 旧版验证成功 → aV.k.a(key) 同步到 V9

V9 激活流程 (aV.k → aV.a → aW):

  aV.k 收到 key → aV.a 处理激活
    ├─ 检查缓存 aV.a.cache (key=产品代码 "93W7")
    ├─ 获取 decoder: aV.k.h['c'] → aW.a("Sample", "A1") → 分隔符 "#A1"
    ├─ 解析 key: 提取前缀 + 混淆数据
    │   ├─ key = "c#A1<混淆数据>"  → 前缀 "c", 分隔符 "#A1"
    │   └─ key = "s#A0<混淆数据>"  → 前缀 "s", 分隔符 "#A0", **绕过 RSA 验证**
    ├─ aW.e.b(混淆数据) 解码混淆层 → JSON (带 "S"+"D" 双字段包装)
    ├─ aW.c.a(JSON) Gson 反序列化 → aV.e { e="S"(签名), f="D"(license 模型) }
    ├─ 构建验证数据: aV_e.b + aV_e.a + aW.c.a(aV_e.f)
    │                  = 前缀 + 分隔符 + license 模型序列化 JSON
    ├─ 如果非 #A0 模式: aW.d.verify(data, aV_e.e) RSA SHA256withRSA 验证
    │   └─ 内部: RSAPublicKeySpec 构造 → initVerify → update(data) → verify(sigB64)
    └─ 结果缓存: aV.a.cache.put("93W7", aV.e)
```

### 6.2 License 加载优先级

```
SetLicenseKey(key)              → 最高优先级，直接使用字符串
    ↓ 未设置
GCEXCEL_JAVA_DEPLOY_LICENSE_V9  → 环境变量
    ↓ 未设置
SetLicenseFile(path)            → 指定文件路径
    ↓ 未设置
{data_dir}/{productUUID}/.license → 默认路径 (优先)
    ↓ 不存在
{data_dir}/{vendorUUID}/.license  → 回退路径
```

### 6.3 RSA 签名验证

```java
// aX.a — 核心签名验证 (from bytecode)
final class aX.a {
    private static PublicKey a();    // 构建 RSA 公钥

    public static boolean a(String source, String sign);  // 验证
    public static byte[] a(String base64);                 // Base64 解码
}
```

**两个硬编码公钥** (取决于 GCLMTEST 环境变量):

| 模式 | 模数 Base64 | 指数 |
|------|-------------|------|
| 生产 | `+US+f7+816LsJEa/Gd0daNSWKehDM7uF72OpCHfSECbjX9WlrrOxIq8kOgIVxkDfJeWmP6OwZw9Xn+VJa8Sxze7jKUopRH1awfK1p+RiQnOcmqUpi4GTUHK+6F9nSc/Y0T5H7pgDNSk8CkB/LwfaaCj0FmrEXns8fguG2l3VrCU=` | `AQAB` (65537) |
| 测试 | `tgoYDy+InG+V+F4gU9ssbjuTHXzHaXwFzyF+SA85fe4AeN1N5jzxA2MzXT8VsArKZ9Ugz2rYPp9kOhpwiSq2QSfxE+axl8403O9JkcB9826e7Co3WjZYOMbfKWLrRJFTWatkEIRJvP2ocOEtYCLDOaET08OCAnSFAcO7fReSd00=` | `AQAB` (65537) |

---

## 7. License 数据解码示例

### 7.1 真实 License (from `license-raw` 任务)

```
数据段:
NmJmNjMwZWEtMjJkMy00N2I1LWJiOWUtMjEwMmYzYzUyMTg2,
NjA2NDExMDdYWFhYWFhYWDA4Mg,
bWFjLW1pbmk,
RmFsc2U,
OTUyNQ,
VHJ1ZQ,
OTUzMg,
OTUzMg,
U3RhbmRhcmQ,
,

签名段:
A7eiXxLGFFM7lGGp+ZPmbntKx/ViM6i1JefDezLXqKzYp39Lc8p7GUe8nDSqv3mmq2TedSW5Fxk7WX3sQzfBgVnzt/pMKod1yTZ7StaS6qD7ytS/zpIrMxMjafnrtjVG4M7ZVpIiSzmLUAxOAMrG9R79ZXLi6ZalDK0PQQe9nOc
```

解码结果：

| # | Base64 | 解码值 | 含义 |
|---|--------|--------|------|
| 0 | `NmJmNjMwZWEtMjJkMy00N2I1LWJiOWUtMjEwMmYzYzUyMTg2` | `6bf630ea-22d3-47b5-bb9e-2102f3c52186` | 产品 UUID |
| 1 | `NjA2NDExMDdYWFhYWFhYWDA4Mg` | `60641107XXXXXXXX082` | 序列号 (X 掩码) |
| 2 | `bWFjLW1pbmk=` | `mac-mini` | 主机名 |
| 3 | `RmFsc2U=` | `false` | 非开发版 |
| 4 | `OTUyNQ==` | `9525` | 激活时间戳 |
| 5 | `VHJ1ZQ==` | `true` | 永久有效 |
| 6 | `OTUzMg==` | `9532` | 过期时间戳 |
| 7 | `OTUzMg==` | `9532` | 冗余 (跳过) |
| 8 | `U3RhbmRhcmQ=` | `Standard` | 版本 |
| 9 | _(空)_ | "" | 保留 |
| 10 | _(空)_ | "" | 保留 |

### 7.2 Fake License (从 `license-fake` 任务)

```
数据段:
NmJmNjMwZWEtMjJkMy00N2I1LWJiOWUtMjEwMmYzYzUyMTg2,
MjE0ODY4NzRYWFhYWFhYWDQ3NDI,
,
dHJ1ZQ,
OTUzMg,
dHJ1ZQ,
MA,
MA,
U3RhbmRhcmQ,
,

签名段:
ISW6b4W0uGAc0SNqCl3MBCgJWAtESWrNciqtmbWF04ccTF6QkB29WotrlUG62ImHRzqrK5ilTtKjt9k7WIHUy/dcRKx46UKtTPePkGVBHoFPhiRukm9ABSbG5brU8sIIU09RdvDbA4GynZxFFSz+br5+ds38Dn5tYJzXhktOKKPlTNxSa+2fyCM2zLvPVYvzz43+T5Tqi6XIZjGOoZUJ4fPgnwb4j9T6IYo+42wIpBbiZiB+YE8EYYBFqOzZvQULRrDTch92tsw8M8FszueT5OuO1Ra/s5r9Iy5F/sluGLkj4to5F+Rg6v9QqtAxnEPbK4BePbEqaEzqbesS7N3pGQ
```

解码结果：

| # | Base64 | 解码值 | 含义 |
|---|--------|--------|------|
| 0 | `NmJmNjMwZWEtMjJkMy00N2I1LWJiOWUtMjEwMmYzYzUyMTg2` | `6bf630ea-22d3-47b5-bb9e-2102f3c52186` | 产品 UUID |
| 1 | `MjE0ODY4NzRYWFhYWFhYWDQ3NDI=` | `21486874XXXXXXXX4742` | 序列号 (X 掩码) |
| 2 | _(空)_ | "" | **主机名为空** 🔑 |
| 3 | `dHJ1ZQ==` | `true` | 开发版 |
| 4 | `OTUzMg==` | `9532` | 激活时间戳 |
| 5 | `dHJ1ZQ==` | `true` | 永久有效 |
| 6 | `MA==` | `0` | **过期时间为 0** 🔑 |
| 7 | `MA==` | `0` | 冗余 (跳过) |
| 8 | `U3RhbmRhcmQ=` | `Standard` | 版本 |
| 9 | _(空)_ | "" | 保留 |
| 10 | _(空)_ | "" | 保留 |

### 7.3 差异对比

| 字段 | 真实 License | Fake License | 破解策略 |
|------|-------------|-------------|----------|
| UUID | `6bf630ea-...` | `6bf630ea-...` | 不变 |
| 序列号 | `60641107XXXXXXXX082` | `21486874XXXXXXXX4742` | 随意生成 |
| 主机名 | `mac-mini` | _(空)_ | **不绑定机器** |
| 开发版 | `false` | `true` | 改为开发版 |
| 激活时间 | `9525` | `9532` | 正常 |
| 永久有效 | `true` | `true` | 不变 |
| 过期时间 | `9532` | `0` | **0 = 永不过期** |
| 版本 | `Standard` | `Standard` | 不变 |

---

## 8. 两层字节码破解机制

### 8.1 层 1: ConstSubstitutionPlugin (LDc 常量池替换)

**目标**: `com.grapecity.documents.excel.internals.aX.a`

**机制**: 在类加载时，ASM 扫描 `aX.a` 方法的 `LDC` 指令，若发现的字符串常量匹配配置中的 `src`，则替换为 `dst`。

```
原始字节码:
  LDC "tgoYDy+InG+V+F4gU9ssbjuTHXzHaXwFzyF+SA85fe4AeN1N5jzxA2MzXT8VsArKZ9Ugz2rYPp9kOhpwiSq2QSfxE+axl8403O9JkcB9826e7Co3WjZYOMbfKWLrRJFTWatkEIRJvP2ocOEtYCLDOaET08OCAnSFAcO7fReSd00="

替换后字节码:
  LDC "AKr5BPhLiCKb3Rc0ZUVFMpSQUYX8CVac2akqS+C24k9eHgLmcTUcmWsuGrPbaAE6uxkEw7LPFJb4EtF0/YdWG7MsJZVOzC7fQ44+nt2L1SOwan5ZFLXlychnLi6VWMdB8d20Trcrq483JtTpWj+Af3rdnEecxijKK6PKDQVXKJPUKg31pHUQBeVUoLJaUpDJtJAyHXp8bY0OUMGp8GCnoF8UPOkCHLtbsx8VrMezfNFoWzoad3Dvg85ebUDJN0qsnmv7V9p+BgiOcuUzVdJ3Xnnv9PVsjm9bm5dWu//NcdrdErIMSpMqZWwaO3KppEokYni5BEvM69jfY5//XRCAYW0="
```

**配置**: `plugin-cs/plugin-cs.toml`
```toml
[[rules]]
class-name = "com.grapecity.documents.excel.internals.aX.a"

[rules.method-info]
access = 10        # private static
name = "a"
descriptor = "()Ljava/security/PublicKey;"

# 测试公钥替换
[[rules.replacers]]
src = "tgoYDy+..."
dst = "AKr5BPh..."

# 生产公钥替换
[[rules.replacers]]
src = "+US+f7+..."
dst = "AJdDILy..."
```

### 8.2 层 2: PublicKeyPlugin (构造函数注入)

**目标**: `java.security.spec.RSAPublicKeySpec.<init>`

**机制**: 通过 ASM Tree API 在 `RSAPublicKeySpec` 构造函数中注入代码，在字段赋值前调用 `ArgsFilter.testFilter()` 替换公钥参数。

```
// 注入后的构造函数等效 java 代码:
public RSAPublicKeySpec(BigInteger modulus, BigInteger exponent, AlgorithmParameterSpec params) {
    BigInteger[] result = ArgsFilter.testFilter(modulus, exponent);
    if (result != null) {
        modulus  = result[0];   // 替换模数
        exponent = result[1];   // 替换指数
    }
    this.modulus = modulus;
    this.exponent = exponent;
    this.params = params;
}
```

### 8.3 `ArgsFilter` — Bootstrap 公钥过滤器

**位置**: `jagent-bootstrap` 模块，由 `Instrumentation.appendToBootstrapClassLoaderSearch()` 加载到 Bootstrap ClassLoader

```java
public class ArgsFilter {
    private static final Set<String> l1cached = new HashSet<>();
    private static final Map<String, BigInteger[]> l2cached = new HashMap<>();

    public static void addRule(String e, String m, String e2, String m2) {
        String key = e + "," + m;
        l1cached.add(key);
        l2cached.put(key, new BigInteger[]{new BigInteger(m2), new BigInteger(e2)});
    }

    public static BigInteger[] testFilter(BigInteger modulus, BigInteger exponent) {
        String key = exponent + "," + modulus;
        if (l1cached.contains(key)) {
            return l2cached.get(key);  // [newModulus, newExponent]
        }
        return null;
    }

    // 注册三条规则: 生产/测试/V9 各一个公钥
    static {
        // 生产公钥 → 自定义公钥 1
        addRule("65537", "1750423157523...", "65537", "1909508603964...");
        // 测试公钥 → 自定义公钥 2
        addRule("65537", "1278324347246...", "65537", "2158330789378...");
        // V9 公钥 → 自定义公钥 3
        addRule("65537", "2346111614314...", "65537", "2158330789378...");
    }
}
```

### 8.4 两层策略对比

| 维度 | ConstSubstitutionPlugin | PublicKeyPlugin |
|------|------------------------|-----------------|
| 目标类 | `com.grapecity...aX.a` | `java.security.spec.RSAPublicKeySpec` |
| 操作 | 替换 LDC 字符串常量 | 注入方法调用到构造函数 |
| 效果 | GcExcel 内部公钥被改 | JDK 密钥构建时参数被换 |
| 技术 | ASM Visitor, `visitLdcInsn()` | ASM Tree API, `InsnList` 注入 |
| 配置 | TOML 驱动, 动态配置 | 硬编码, 需重新编译 |
| 是否需要 Bootstrap | 否 | 是 (ArgsFilter 需 Bootstrap CL) |

> ℹ️ `RSAPublicKeySpec` 的 2 参数构造函数 `(BigInteger, BigInteger)` 内部会调用 3 参数构造函数 `(BigInteger, BigInteger, AlgorithmParameterSpec)`。因此 `PublicKeyPlugin` 仅需拦截 3 参数构造函数即可覆盖新旧两套系统 (以及任何其他 `RSAPublicKeySpec` 构造路径)。

---

## 9. jagent 字节码框架架构

### 9.1 类加载体系

```
Bootstrap ClassLoader
  └── jagent-bootstrap-0.1.0.jar
        ├── ArgsFilter         ← 注入到 RSAPublicKeySpec 的过滤器
        └── Licence             ← License 格式校验 (未完成)

System ClassLoader
  ├── gcexcel-9.0.1.jar        ← 目标库
  │   └── aX.a                 ← 被常量替换攻击
  ├── JAgent-1.0.0.jar         ← Agent 框架
  │   └── PluginManager        ← ServiceLoader 发现插件
  └── plugin-cs-0.1.0.jar      ← 常量替换插件
```

### 9.2 Agent 初始化流程

```
JVM 启动 → premain(Instrumentation inst)
  → Launcher.premain()
  → Initializer.processAgent()
      ├─ 定位 jagent-bootstrap-*.jar
      ├─ inst.appendToBootstrapClassLoaderSearch(jar)
      ├─ 创建 Environment(inst, agentJar, ...)
      ├─ 创建 Agent (implements ClassFileTransformer)
      ├─ PluginManager.loadPlugins(appContext)
      │   ├─ 扫描 plugins/ 目录 JAR
      │   ├─ ServiceLoader<Plugin> 发现插件
      │   ├─ 加载 TOML 配置
      │   ├─ plugin.init(appContext, config)
      │   └─ 注册 ITransformer 到 Agent
      ├─ inst.addTransformer(agent, true)   ← 注册转换器
      └─ 对已加载类触发 retransform
```

### 9.3 类转换管道

```
JVM 加载类 → agent.transform(loader, className, ...)
  ├─ className 匹配 transformerMap?
  ├─ 按 getOrder() 排序所有匹配的 ITransformer
  └─ 链式应用:
      └─ ITransformer.getCode(bytes, order)
           └─ ASMTransformer.getCode() (默认实现)
                ├─ ClassReader(bytecode)
                ├─ ClassWriter(COMPUTE_FRAMES)
                ├─ getClassVisitor(writer)
                │   └─ ConstSubstitutionVisitor
                │       └─ 匹配方法 → MethodConstantVisitor
                │           └─ visitLdcInsn() 替换常量
                └─ reader.accept(visitor, SKIP_DEBUG|SKIP_FRAMES)
```

---

## 10. 关键类索引

### GcExcel 内部类 (obfuscated)

| 内部类 | 系统 | 描述 | 关键方法 |
|--------|------|------|---------|
| `aU.a` | 通用 | License 验证结果枚举 (6 值) | — |
| `aU.b` | 通用 | 抽象授权评估器 | `a()→aU.a`, `b()→aU.f`, `c()→boolean` |
| `aU.c` | V9 | 评估状态包装器 (wraps aV.k) | — |
| `aU.d` | 旧版 | 解析结果包装器 (wraps aX.c) | — |
| `aU.e` | **入口** | **授权评估入口** | `a()→aU.b`, `a(String)` SetLicenseKey, `b(String)` SetLicenseFile |
| `aU.f` | 通用 | License 类型枚举 (3 值) | — |
| `aV.a` | V9 | **激活控制器** | 管理 decoder/verifier map, 缓存 activations |
| `aV.b` | V9 | **激活提供者接口** | `a()→key`, `b()→name`, `c()→aV.h`, `d()→bool`, `e()→days`, `f()→products`, `g()→perpetual` |
| `aV.c` | V9 | **产品接口** | `a()→productCode`, `b()→name`, `c()→key`, `a(String)` setKey, `d()→Event` |
| `aV.d` | V9 | License 变更监听器 | (event handler) |
| `aV.e` | V9 | **激活缓存条目** | `a→分隔符`, `b→前缀`, `c→域名[]`, `d→计数`, `e→签名`, `f→aV.f` |
| `aV.f` | V9 | **结构化 license 模型** | `Id/Evl/OId/CNa/CId/Dms/Ips/Exp/Crt/Prd/Anl` |
| `aV.g` | V9 | License 异常 | — |
| `aV.h` | V9 | 授权状态枚举 (3 值) | `Evaluation`, `Unlicensed`, `Licensed` |
| `aV.i` | V9 | 产品信息 | `a=N(name)`, `b=C(code)` |
| `aV.k` | V9 | **V9 状态管理器** | `e()→aV.h`, `a()→"93W7"`, `b()→"localhost"`, `f()→bool`, `g()→bool` |
| `aW.a` | V9 | **Crypto 编码器** | `a()/b() keys`, `a(String) encode`, `b(String) decode` |
| `aW.b` | V9 | 事件处理器接口 | `a(Object, EventArgs)` |
| `aW.c` | V9 | **Gson JSON 序列化/反序列化** | `a(aV.f)→JSON`, `a(String)→aV.e` |
| `aW.d` | V9 | **RSA 签名验证器** | `a(String modulus, String exp)`, `a(data, sig)→boolean` |
| `aW.e` | V9 | **4 层混淆编码层** | `a(s) encode`, `b(s) decode` |
| `aZ.m` | 通用 | 功能模块枚举 (4 值) | — |
| `aX.a` | 旧版 | **RSA 签名验证** | `a()→PublicKey`, `a(String,String)→boolean` |
| `aX.b` | 旧版 | **License 加载管理器** | `a()→aX.c`, `a(String)` SetLicenseKey, `b(String)` SetLicenseFile |
| `aX.c` | 旧版 | **解析后授权对象** | 见 3.1 节 |
| `aX.d` | 旧版 | 解析结果单例 | `a()→aX.e`, `a(aX.e)`, `b()` reset |
| `aX.e` | 旧版 | 解析结果枚举 (4 值) | — |
| `aX.f` | 旧版 | UUID & 路径管理 | `a()→boolean` GCLMTEST, `b()/c()→UUID` |
| `F.bR` | 通用 | **JSON→Java 展平工具** | `a(JsonElement)→Object`, `a(String,boolean)→Object` |
| `F.bL` | 通用 | LinkedTreeMap + JsonElement 包装 | 实现 `F.bu` 接口 |

### 项目自定义类

| 类 (模块) | 描述 |
|-----------|------|
| `GCDemo` (gcexcel) | 入口 Demo |
| `JsonDS` (gcexcel) | 自定义 `DataSource.l`, 用 Gson 序列化 Java POJO |
| `LicenseMange` (gcexcel) | License 解析/签名验证/破解工具 |
| `Other` (gcexcel) | 反射打印内部 license 状态枚举 |
| `Launcher` (jagent) | Agent 入口 `premain()`/`agentmain()` |
| `Initializer` (jagent) | 初始化, 加载 bootstrap, 注册插件 |
| `AbstractAgent` (jagent) | `ClassFileTransformer` 实现, transformer 链 |
| `ASMTransformer` (jagent) | ASM 转换接口, ClassReader/Writer 骨架 |
| `BaseClassVisitor` (jagent) | 方法匹配 + 委托修改的 ASM Visitor 基类 |
| `PluginManager` (jagent) | SPI 插件发现 + TOML 配置 + 注册 |
| `ArgsFilter` (jagent-bootstrap) | Bootstrap 类, RSA 公钥运行时替换 |
| `ConstSubstitutionPlugin` (plugin-cs) | 常量替换插件入口 |
| `ConstSubstitutionVisitor` (plugin-cs) | LDC 指令拦截/替换 |
| `PublicKeyPlugin` (plugin-cs) | 构造函数注入 RSAPublicKeySpec |

---

## 11. 时序图: 完整启动流程

```
JVM
  │
  ├── 1. premain() → Launcher
  │     └── Initializer.processAgent(inst)
  │           ├── 2. Bootstrap CL 加载 ArgsFilter
  │           ├── 3. 创建 Agent (transformer map 为空)
  │           ├── 4. PluginManager 扫描 plugins/
  │           │     └── ServiceLoader 发现 ConstSubstitutionPlugin
  │           │           ├── 5. 加载 plugin-cs.toml
  │           │           │     └── 解析出 rule: aX.a / method:a / 2 个 replacer
  │           │           └── 6. 注册 ConstSubstitutionTransformer → Agent
  │           ├── 7. 注册 agent 到 inst.addTransformer(agent)
  │           └── (attach 模式下 retransform 已加载类)
  │
  ├── 8. 类加载: com.grapecity...aX.a
  │     └── agent.transform()
  │           └── ConstSubstitutionTransformer
  │                 └── visitLdcInsn() 替换公钥字符串 (旧版公钥 👉 有效)
  │
  ├── 9. 类加载: java.security.spec.RSAPublicKeySpec
  │     └── PublicKeyTransformer.getCode()
  │           └── 注入 ArgsFilter.testFilter() 调用
  │                 └── 覆盖旧版 aX.a + V9 aW.d 构造函数 (均使用 RSAPublicKeySpec)
  │                       └── 两层防御均有效 ✅
  │
  ├── 10. GCDemo.main()
  │      └── new Workbook()
  │            └── aU.e.a()
  │                  ├── (a) V9 路径: aV.k.a(key) → aV.a → aW.c → aW.d.verify()
  │                  │     └── aW.d 内部: new RSAPublicKeySpec(modulus, exponent)
  │                  │           └── ArgsFilter.testFilter() → 公钥已替换 ✅
  │                  │
  │                  └── (b) 旧版路径: aX.b → aX.c → aX.a.a(data, sign)
  │                        └── aX.a 内部: new RSAPublicKeySpec(modulus, exponent)
  │                              └── ArgsFilter.testFilter() → 公钥已替换 ✅
  │
  └── 11. wb.processTemplate() → 模板填充 ✅
```

---

## 12. 附录: 关键 RSA 参数

### 生产公钥对

```
原始 (GcExcel 官方):
  模数:  175042315752321278209834854482966386728768369474963874601331848179232395698487140876837125192224976487154136741395513778301647277257327777948881794629594126177287043936129382281014872710276097795554993535873740962335166325202031649639739587187554224357527189779251109312841495592630999010577058977147325361189
  指数: 65537

替换后 (自定义):
  模数:  19095086039647125457676330212105742470637615492780479281805441679697788852894346663448847011496474138422426165303096676769716161636660763611604168628997868013161740550968654398124921012995869915192534308390790683621482875465920419213734973554065733085986602469079080143863728883879196577986174043629978537322442179531083021298364360152447772371707050178730850597976325429679668831602221116003020980194780125153267981301113652618913394032274156442363005110868789577519369208041648053567779572818909342600354791974128005967858001044214077473995402434992870401763443170354698084174484661453032623951025271227457642844101
  指数: 65537
```

### 测试公钥对

```
原始 (GcExcel 测试):
  模数:  127832434724636252675860241718522532046750371259051847265456202494709997975196097266670200801996028474680495148334641450476513504035485374783856760140674181144339590013648333843059252299332477649159502959225928879560196826006742441877093677887884072437762643739225248614280244498513170239774435025090537224013
  指数: 65537

替换后 (自定义):
  模数:  21583307893787870603277507289554717529088421164295711628543487312692002376547319174148283709379869013853618715921036716314138607367629301333294673019742284855264052916321618230109827845259894927992919707220738362848937656624107669055730464594125160949507064846497016895850411256089184256702924543582560121706785062633110013783648515963298915662683929929935844342573752474202870199251727718705336540395423240958247642457737906934221783789357206264592602610647357708613135090640189377493585789073912232135887253846720115408838019887133851844790382918833940516674082278762680473765979455974227814407429067890906535846253
  指数: 65537
```

### V9 公钥对 (aW.d / aV.k)

```
原始 (GcExcel V9, 硬编码在 aV.k.a(Character)):
  模数 Base64: udkL9Yp1FotxuamEH+Q3l7d6r+PAhQ6YypLjvRCINZ19FKWe4DiN4VCRkxrP4kPaubPzoi0WNHThfTzrc8PToRuzQmbCxsvb1IsS6rOssOjFVvT09InQw5jogiY9nnVnA0lgmHd43KY1jvkiLV/iK+h4n0tz9w9D812eL6RVOYKzeN+uXsdOxrS01D+liBPx3UGfcNYW/jY5me7OQQPB5I16zvmifWqr68EVuRchI5xc+nDzu6lZghrMl33Lsbx8JljQiXmUi2PGSdXmj6adqPjeyLdHlPWuFMUEljYgVgaxAKxGBNyPm08lVMFnTpJUpQLB9Inc51itnFrob9wpEQ==
  指数: AQAB (65537)
```

> ✅ V9 独立公钥已通过 `ArgsFilter` 第 3 条规则覆盖。`RSAPublicKeySpec` 的 2 参数构造函数内部会调用 3 参数构造函数，因此 `PublicKeyPlugin` 的 `RSAPublicKeySpec.<init>` 拦截对所有 GcExcel 公钥构造均有效。
> 
> 替换后同样使用自定义公钥对（与旧版替换公钥相同，见上）。
