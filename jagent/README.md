# JAgent — Java Instrumentation Agent Framework

JAgent 是一个基于 JVM Instrumentation API 的字节码增强框架，支持 `-javaagent` 启动时增强和动态 Attach 热插拔两种模式。提供插件化架构，允许通过 SPI 机制动态加载自定义字节码转换器。

---

## 特性

- **双模式增强**：支持 premain（启动时）和 agentmain（运行时 Attach）两种模式
- **插件化架构**：基于 Java `ServiceLoader` 的 SPI 插件发现机制，即插即用
- **ASM 字节码操作**：基于 ASM 框架，提供 `ASMTransformer` 等基类简化转换器开发
- **TOML 配置**：插件使用 TOML 格式配置文件，支持 `disabled` 开关和自定义规则
- **Bootstrap 隔离**：核心过滤/验证逻辑通过 Bootstrap ClassLoader 加载，对应用完全透明
- **多环境支持**：通过 Agent 参数指定应用名称，自动隔离不同应用的配置/日志/插件目录

---

## 快速开始

### 使用预构建包

1. 从构建产物获取 `JAgent.jar` 及配套插件
2. 添加 JVM 参数启动：

```bash
java -javaagent:/path/to/JAgent.jar -jar your-app.jar
```

3. 或通过 Attach 模式动态挂载：

```bash
java -jar /path/to/JAgent.jar
```

### Agent 参数

通过 `-javaagent` 的 `=` 后指定应用名称，实现多环境隔离：

```bash
java -javaagent:/path/to/JAgent.jar=myapp -jar your-app.jar
```

此时配置/日志/插件目录分别为 `conf-myapp/`、`logs-myapp/`、`plugins-myapp/`。

---

## 构建

### 环境要求

- JDK 8+
- Gradle 8.x

### 编译

```bash
# 编译全部模块
./gradlew :distribution:dist

# 或单独编译 jagent
./gradlew :jagent:shadowJar
```

### 产物结构

构建产物位于 `distribution/build/dist/`：

```
dist/
├── JAgent.jar                  # Agent 主 JAR（Shadow 打包）
├── jagent-bootstrap-0.1.0.jar # Bootstrap 扩展 JAR
├── README.md
├── conf/
│   └── plugin-cs.toml          # 常量替换插件配置
└── plugins/
    ├── plugin-ep.jar            # 环境公钥适配插件
    ├── plugin-cs.jar            # 常量替换插件
    └── plugin-timing.jar        # 方法耗时统计插件
```

---

## 模块架构

```
jtools/
├── jagent/                     # Agent 核心框架
│   ├── api/                    # 插件 SPI 接口定义
│   ├── core/                   # 核心实现
│   │   ├── plugin/             # 插件管理器
│   │   └── asm/visitor/        # ASM 字节码访问器基类
│   └── utils/                  # 工具类
├── jagent-bootstrap/           # Bootstrap 类加载器扩展（核心过滤逻辑）
├── plugin-ep/                  # 环境公钥适配插件
├── plugin-cs/                  # 常量替换插件
├── plugin-timing/              # 方法耗时统计插件
└── distribution/               # 分发包组装
```

---

## 配置

### 系统属性

| 属性 | 说明 | 默认值 |
|------|------|--------|
| `janf.debug` | 调试日志级别 (0-4) | `0` |
| `janf.output` | 日志输出方式 | `1` (CONSOLE) |
| `class.pattern.include` | 类名包含匹配正则 | 全部 |
| `class.pattern.exclude` | 类名排除匹配正则 | 无 |
| `class.out.dir` | 转换后的类文件输出目录 | `~/code/` |

日志级别：`NONE=0`、`DEBUG=1`、`INFO=2`、`WARN=3`、`ERROR=4`

日志输出：`NONE=0`、`CONSOLE=1`、`FILE=2`、`CONSOLE+FILE=3`、`WITH_PID=4`

### 环境变量

| 变量 | 说明 |
|------|------|
| `JANF_DEBUG` | 同 `janf.debug` |
| `JANF_OUTPUT` | 同 `janf.output` |
| `JAVA8_HOME` | JDK 8 路径（编译 tools.jar 所需） |

---

## 插件系统

### 插件发现

插件 JAR 放入 `plugins/` 目录后，`PluginManager` 通过 `ServiceLoader` 自动发现 `Plugin` 接口实现。文件后缀为 `.disabled.jar` 时插件会被禁用。

### 开发插件

1. 引入 `jagent` 依赖
2. 实现 `Plugin` 接口（或继承 `BasePlugin`）
3. 实现 `ITransformer` 接口处理字节码转换
4. 使用 `@AutoService(Plugin.class)` 注册 SPI
5. 在 `META-INF/MANIFEST.MF` 中声明插件元信息

### 现有插件

| 插件 | 说明 |
|------|------|
| **plugin-ep** | 环境公钥适配 — 运行时动态注入 RSA 公钥参数，支持多环境（开发/测试/生产）切换 |
| **plugin-cs** | 常量替换 — 按规则替换指定类的常量池 LDC 值，支持方法级别的精确匹配 |
| **plugin-timing** | 方法耗时统计 — 开发中，骨架已搭建 |

---

## 配置格式

插件使用 TOML 格式配置，位于 `conf/` 目录下：

```toml
disabled = false

[[rules]]
class-name = "com.example.TargetClass"
method-info = "public static void main(String[] args)"
[[rules.replacers]]
source = "oldValue"
target = "newValue"
```

---

## 许可

本项目仅供学习和研究使用。
