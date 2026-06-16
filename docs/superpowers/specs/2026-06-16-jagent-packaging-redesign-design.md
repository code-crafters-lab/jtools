# JAgent 打包重构设计

**日期**：2026-06-16
**状态**：已确认，待实施
**范围**：jtools 仓库内 `jagent/`、`distribution/`、`jagent-bootstrap/`（将合并）、`plugin-cs/`、`plugin-ep/`、`plugin-timing/` 模块

---

## 1. 背景与目标

### 当前状态

`distribution/build/dist/` 现有产物：

```
JAgent-1.0.0.jar              # shadow jar（含 slf4j/asm/toml4j 等所有依赖）
jagent-bootstrap-0.1.0.jar    # 共享 Bootstrap JAR，位于根目录
README.md
conf/                         # 空（plugin-cs.toml 未被复制）
plugins/
  └── plugin-ep-0.1.0.jar     # 唯一已包含的插件
```

### 痛点

1. **主 JAR 体积臃肿**：所有第三方依赖被打入 shadow jar，难以独立升级。
2. **Bootstrap 机制粗糙**：硬编码 `name.endsWith("jagent-bootstrap-0.1.0.jar")`（`Initializer.java:49`），扩展性差。
3. **模块边界混乱**：`jagent-bootstrap` 是独立模块，但本质是 JAgent 核心的一部分。
4. **插件分发不全**：`plugin-cs`、`plugin-timing` 未被纳入 distribution。
5. **`conf/` 为空**：`plugin-cs.toml` 没被复制到产物。

### 目标产物结构

```
dist/
├── JAgent-x.x.x.jar                  # Agent 主 JAR（不再 shadow）
├── JAgent-x.x.x.bootstrap.jar        # 核心 Bootstrap 切片
├── libs/                             # 核心第三方依赖（外部化）
│   ├── slf4j-api-x.x.x.jar
│   ├── asm-x.x.x.jar
│   ├── asm-commons-x.x.x.jar
│   ├── asm-util-x.x.x.jar
│   └── toml4j-x.x.x.jar
├── plugins/
│   ├── plugin-ep-0.1.0.jar
│   ├── plugin-ep-0.1.0.bootstrap.jar
│   ├── plugin-cs-0.1.0.jar
│   ├── plugin-cs-0.1.0.bootstrap.jar
│   ├── plugin-timing-0.1.0.jar
│   └── plugin-timing-0.1.0.bootstrap.jar
├── README.md
└── conf/
    └── plugin-cs.toml
```

---

## 2. 关键决策

| 决策 | 选项 | 理由 |
|---|---|---|
| `libs/` 含义 | A — 停止 Shadow 打包，外部化依赖 | 主 JAR 保持精简，依赖可独立升级 |
| `.bootstrap.jar` 语义 | X — 每插件各自的 Bootstrap 变体 | 新机制，插件自治，与核心 bootstrap 隔离 |
| 依赖加载方式 | R — Manifest `Class-Path:` + URLClassLoader 双管齐下 | 核心依赖走 manifest（稳定），插件私有依赖走 URLClassLoader（灵活） |
| `jagent-bootstrap` 模块 | O — 合并到 `jagent/core`，产出 `JAgent-x.x.x.bootstrap.jar` | 单一核心来源，模块边界清晰 |

---

## 3. 模块重构

### 3.1 移除 `jagent-bootstrap` 独立模块

删除顶层 `jagent-bootstrap/` 目录及其在 `jtools/settings.gradle.kts`、`distribution/build.gradle.kts` 中的引用。

源码迁移：

| 原路径 | 新路径 |
|---|---|
| `jagent-bootstrap/src/main/java/org/codecrafterslab/agent/Licence.java` | `jagent/core/src/main/java/org/codecrafterslab/agent/Licence.java` |
| `jagent-bootstrap/src/main/java/org/codecrafterslab/agent/plugin/ArgsFilter.java` | `jagent/core/src/main/java/org/codecrafterslab/agent/plugin/ArgsFilter.java` |
| `jagent-bootstrap/src/main/java/org/codecrafterslab/agent/plugin/PairFinger.java` | `jagent/core/src/main/java/org/codecrafterslab/agent/plugin/PairFinger.java` |

包路径保持不变（`org.codecrafterslab.agent.*`），确保外部引用兼容。

测试用例 `LicenceTest.java` 一并迁移到 `jagent/core/src/test/...`。

### 3.2 `jagent/core` Gradle 改造

**`jagent/core/build.gradle.kts` 改动**：

1. 保留 `com.gradleup.shadow` 插件但**不再调用** `shadowJar` 任务（作为可选回退，见 §6.3）。
2. **移除** 普通 `Jar` 任务的 `archiveClassifier.set("original")`（原 `JAgent-original.jar` 不再需要）。
3. 新增 `bootstrapJar` 任务，仅包含 `ArgsFilter`、`PairFinger`、`Licence` 三个类：
   ```kotlin
   tasks.register<Jar>("bootstrapJar") {
       archiveBaseName.set("JAgent")
       archiveClassifier.set("bootstrap")
       from(sourceSets.main.get().output)
       include("org/codecrafterslab/agent/Licence.class")
       include("org/codecrafterslab/agent/plugin/ArgsFilter.class")
       include("org/codecrafterslab/agent/plugin/PairFinger.class")
       manifest {
           attributes(
               "Plugin-Name" to "jagent-core-bootstrap",
               "Plugin-Bootstrap-Required" to "true",
               "Plugin-Bootstrap-Priority" to "0"
           )
       }
   }
   ```
4. 主 `Jar` 任务的 manifest 增加 `Class-Path:`（不设置 classifier）：
   ```kotlin
   tasks.withType<Jar>().configureEach {
       archiveBaseName.set("JAgent")
       manifest {
           attributes(
               "Premain-Class" to "org.codecrafterslab.agent.Launcher",
               "Agent-Class" to "org.codecrafterslab.agent.Launcher",
               "Main-Class" to "org.codecrafterslab.agent.Usage",
               "Class-Path" to listOf(
                   "libs/slf4j-api-${libs.versions.slf4j.get()}.jar",
                   "libs/asm-${libs.versions.asm.get()}.jar",
                   "libs/asm-commons-${libs.versions.asm.get()}.jar",
                   "libs/asm-util-${libs.versions.asm.get()}.jar",
                   "libs/toml4j-${libs.versions.toml4j.get()}.jar"
               ).joinToString(" ")
           )
       }
   }
   ```

最终 `core/build/libs/` 产出两个 JAR：
- `JAgent-${version}.jar` — 主 Agent 代码
- `JAgent-${version}-bootstrap.jar` — Bootstrap 切片

`shadowJar` 任务保留但不执行；`withType<Jar>().configureEach` 会把 `archiveBaseName` 同步应用到 `shadowJar`（无副作用）。

### 3.3 插件模块（`plugin-ep`、`plugin-cs`、`plugin-timing`）改造

**每个插件 Gradle 改动**（以 `plugin-cs` 为例）：

```kotlin
tasks {
    withType<Jar>().configureEach {
        manifest {
            attributes(
                "Plugin-Name" to project.name,
                "Plugin-Version" to project.version,
                "Plugin-Bootstrap-Required" to "true",
                "Plugin-Bootstrap-Priority" to "10"
            )
        }
    }
    register<Jar>("bootstrapJar") {
        archiveClassifier.set("bootstrap")
        from(sourceSets.main.get().output)
        // 仅包含该插件需要在 Bootstrap CL 可见的字节码 hook 类
        // （不包含 core-bootstrap 已提供的 ArgsFilter/PairFinger/Licence，
        //  这些由 JAgent-x.x.x.bootstrap.jar 优先加载）
        include("org/codecrafterslab/agent/plugin/cs/ConstSubstitutionVisitor.class")
    }
}
```

`include` 列表按插件实际需求定制：仅含 Bootstrap ClassLoader 可见的字节码 hook 类。`ArgsFilter` / `PairFinger` / `Licence` 由 `JAgent-x.x.x.bootstrap.jar` 优先加载（`Plugin-Bootstrap-Priority=0`），各插件无需重复打包。

### 3.4 `plugin-cs` 纳入构建

**`jtools/settings.gradle.kts`** 修改：
- 当前：`includeBuild("jagent")`，未 include `plugin-cs`。
- 修改：保留 `includeBuild("jagent")`，新增 `includeBuild("plugin-cs")` 或迁移到 `jagent/plugins/plugin-cs/`，统一管理。

**`jagent/settings.gradle.kts`** 修改：
- 当前：注释 `include("plugin-cs")`。
- 修改：去掉注释，使 `plugin-cs` 成为 `jagent` composite build 的子项目。

---

## 4. `Initializer.java` 改造

**文件**：`jagent/core/src/main/java/org/codecrafterslab/agent/Initializer.java`

**变更要点**：

1. 删除第 49 行硬编码 `name.endsWith("jagent-bootstrap-0.1.0.jar")`。
2. 新增逻辑：扫描 `plugins/*.bootstrap.jar`，按 Manifest 的 `Plugin-Bootstrap-Priority` 升序，逐个 `appendToBootstrapClassLoaderSearch`。
3. 新增逻辑：创建 `URLClassLoader`，扫描 `plugins/*.jar`（排除 `.bootstrap.jar` 和 Agent 主 JAR 本身），作为 `PluginManager` 的 `ServiceLoader` 来源。
4. `Environment` 类扩展 `pluginClassLoader` 字段。

**伪代码**（第 46-61 行替换）：

```java
AgentUtil.getAgentJarFile().ifPresent(file -> {
    File distRoot = file.getParentFile();             // dist/ 根目录
    File pluginsDir = new File(distRoot, "plugins");  // dist/plugins/
    loaded = true;

    // 1. Bootstrap CL：合并根目录与 plugins/ 下的 *.bootstrap.jar，按优先级升序加载
    File[] coreBootstraps = Optional.ofNullable(distRoot.listFiles(
        (dir, name) -> name.endsWith(".bootstrap.jar"))).orElse(new File[0]);
    File[] pluginBootstraps = Optional.ofNullable(pluginsDir.listFiles(
        (dir, name) -> name.endsWith(".bootstrap.jar"))).orElse(new File[0]);
    Stream.concat(Arrays.stream(coreBootstraps), Arrays.stream(pluginBootstraps))
        .sorted(Comparator.comparingInt(Initializer::readBootstrapPriority))
        .map(Initializer::openJarSafely)
        .filter(Objects::nonNull)
        .forEach(inst::appendToBootstrapClassLoaderSearch);

    // 2. AppClassLoader 子：加载 plugins/*.jar（不含 bootstrap 变体，不含 Agent 主 JAR）
    URLClassLoader pluginCl = buildPluginClassLoader(pluginsDir, file);

    Environment environment = new Environment(inst, file, agentArgs, attach, pluginCl);
    Initializer.init(log, environment);
});
```

辅助方法：

```java
private static int readBootstrapPriority(File jar) {
    try (JarFile jf = new JarFile(jar)) {
        String p = jf.getManifest().getMainAttributes()
            .getValue("Plugin-Bootstrap-Priority");
        return p == null ? Integer.MAX_VALUE : Integer.parseInt(p);
    } catch (Exception e) {
        return Integer.MAX_VALUE;
    }
}

private static URLClassLoader buildPluginClassLoader(File pluginsDir, File agentJar) {
    File[] jars = Optional.ofNullable(pluginsDir.listFiles(
        (dir, name) -> name.endsWith(".jar")
            && !name.endsWith(".bootstrap.jar"))).orElse(new File[0]);
    URL[] urls = Arrays.stream(jars)
        .map(f -> { try { return f.toURI().toURL(); } catch (Exception e) { return null; }})
        .filter(Objects::nonNull)
        .toArray(URL[]::new);
    return new URLClassLoader(urls, Initializer.class.getClassLoader());
}
```

注：Agent 主 JAR（`file`）通过 JVM `-javaagent` 机制已经可见，无需再加入 `pluginClassLoader` 的 URLs。`buildPluginClassLoader` 仅扫描 `plugins/*.jar`，过滤掉 `*.bootstrap.jar` 与 Agent 主 JAR 同名变体。

### 4.1 `Environment` 扩展

**文件**：`jagent/core/src/main/java/org/codecrafterslab/agent/core/Environment.java`

新增字段：
```java
private final URLClassLoader pluginClassLoader;
```

构造器新增参数（带默认值 `null` 以保持向后兼容）；提供 getter 供 `PluginManager.loadPlugins` 使用。

**影响范围**：构造器新增可选参数，不删除旧构造器。调用方包括但不限于：
- `Initializer.java:59`（必须更新）
- 任何测试代码或 demo（保持兼容）

### 4.2 `PluginManager.loadPlugins` 调整

**文件**：`jagent/core/src/main/java/org/codecrafterslab/agent/core/plugin/PluginManager.java`

`ServiceLoader.load(Plugin.class, environment.getPluginClassLoader())` 替代默认 ClassLoader。当 `pluginClassLoader` 为 `null` 时回退到 `ServiceLoader.load(Plugin.class)`（旧行为），保证测试与 demo 不被破坏。

---

## 5. `distribution/build.gradle.kts` 改造

**新增 `libs` Copy 任务**：

```kotlin
register<Copy>("libs") {
    group = "distribution"
    dependsOn(":jagent:core:build")

    // 从 jagent:core 的 runtimeClasspath 提取依赖 JAR
    val coreProject = project(":jagent:core")
    val runtimeFiles: FileCollection = coreProject.files(
        coreProject.configurations.runtimeClasspath
    )
    from(runtimeFiles)
    into(project.layout.buildDirectory.dir("dist/libs"))
    include("*.jar")
}
```

注：`FileCollection` 显式包装避免 Gradle 8.x 在 `Copy.from(Configuration)` 上的类型推断歧义；运行时 `runtimeClasspath` 仅含第三方 JAR（slf4j / asm / toml4j），不含 `jagent` 自身。

**改造 `entry` 任务**：

```kotlin
register<Copy>("entry") {
    group = "distribution"
    dependsOn(clean, ":jagent:core:build")
    val mainProject = project(":jagent:core")
    from(mainProject.layout.buildDirectory.file("libs"))
    from(mainProject.layout.projectDirectory.file("README.md"))
    into(project.layout.buildDirectory.dir("dist"))
    include("JAgent-*.jar")          // 主 JAR + bootstrap JAR
    include("README.md")
}
```

**改造各插件 Copy 任务**：

```kotlin
register<Copy>("plugin-cs") {
    dependsOn(":jagent:plugins:plugin-cs:build")
    val p = project(":jagent:plugins:plugin-cs")
    from(p.layout.buildDirectory.file("libs"))
    into(project.layout.buildDirectory.dir("dist/plugins"))
}
```

同理改造 `plugin-ep`、`plugin-timing`。

**修复 `plugin-cs-conf` 任务**：确保 `plugin-cs.toml` 路径正确（迁移后可能变为 `jagent/plugins/plugin-cs/plugin-cs.toml`）。

**`dist` 主任务依赖链**：

```kotlin
register("dist") {
    group = "distribution"
    dependsOn(clean, "libs", "entry", "plugin-ep", "plugin-cs", "plugin-cs-conf", "plugin-timing")
}
```

---

## 6. 测试与验证

### 6.1 单元测试

- `InitializerTest`：mock `Instrumentation` 与文件系统，验证：
  - 无 `*.bootstrap.jar` 时不报错
  - 多 `.bootstrap.jar` 按 `Plugin-Bootstrap-Priority` 升序加载
  - 主 JAR 不被加入 `pluginClassLoader`

- `EnvironmentTest`：验证 `pluginClassLoader` 字段正确传递。

- `PluginManagerTest`：验证 `ServiceLoader` 使用 `pluginClassLoader` 发现插件。

### 6.2 集成测试

1. `./gradlew :distribution:dist` 完整构建。
2. 验证 `dist/` 产物结构与本设计 §1 一致。
3. 使用 `java -javaagent:dist/JAgent-1.0.0.jar=demo -jar demo.jar` 验证：
   - 启动日志显示 `Bootstrap CL loaded: jagent-core-bootstrap, plugin-cs, ...`
   - 插件的 `ConstSubstitutionVisitor` 在被 instrumentation 类中可见
4. Attach 模式测试：`java -jar dist/JAgent-1.0.0.jar <pid>`。

### 6.3 回退路径

若新打包在集成测试中发现问题，可临时回退：

```kotlin
// jagent/core/build.gradle.kts
if (project.findProperty("shadow.fallback") == "true") {
    tasks.named("shadowJar") { enabled = true }
}
```

`./gradlew -Pshadow.fallback=true :distribution:dist` 重新启用 shadow。

---

## 7. 风险与缓解

| 风险 | 缓解 |
|---|---|
| Manifest `Class-Path:` 不支持通配符，依赖版本变化需重新生成 | Gradle 任务里根据 `libs.versions.toml` 自动生成 `Class-Path` |
| URLClassLoader 与 `-javaagent` 的 premain 启动顺序在某些 JVM 上有微妙差异 | 在 `premain` 入口完成所有 ClassLoader 初始化；保留回退到抛错并打印明确日志 |
| `plugin-cs` 迁移路径未确定（保留顶层 vs 移入 jagent/plugins/） | 优先选择"保留顶层 + includeBuild"以最小化改动 |
| 现有调用方依赖 `jagent-bootstrap` 模块坐标 | 通过 Maven Publish 发布新坐标（如 `jagent-core-bootstrap`），或保留旧坐标重定向 |

---

## 8. 实施顺序（高层）

1. 迁移 `jagent-bootstrap` 三个类到 `jagent/core`，并删除原模块目录及其在 `jtools/settings.gradle.kts` 中的引用。
2. 修改 `jagent/core/build.gradle.kts`：新增 `bootstrapJar`、主 JAR 加 `Class-Path:`。
3. 修改 `Initializer.java` 加载逻辑（删除硬编码、新增 `readBootstrapPriority` / `buildPluginClassLoader`）。
4. 扩展 `Environment.java`（新增可选构造参数）、`PluginManager.java`（使用 `pluginClassLoader`）。
5. 各插件模块（`plugin-ep`、`plugin-cs`、`plugin-timing`）增加 `bootstrapJar` 任务。
6. 修复 `jtools/settings.gradle.kts`、`jagent/settings.gradle.kts` 使 `plugin-cs` 被纳入构建。
7. 改造 `distribution/build.gradle.kts`：新增 `libs`、调整 `entry`、调整各插件 Copy 任务。
8. 编写/迁移测试用例（`InitializerTest`、`EnvironmentTest`、`PluginManagerTest`）。
9. 完整构建并运行集成验证。

---

## 9. 不在范围内（YAGNI）

- 插件签名/校验机制（Manifest 里加签名属性等）
- 插件热加载/热卸载
- 插件依赖冲突检测
- 远程仓库拉取插件

上述功能不在本次重构范围内，留待后续版本。
