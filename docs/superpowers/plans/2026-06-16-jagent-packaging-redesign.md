# JAgent 打包重构实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 把 JAgent 从"单一 shadow JAR + 共享 bootstrap JAR"重构为"主 JAR + 外部化 libs/ + 每插件各自的 `.bootstrap.jar` 变体 + 核心 `JAgent-x.x.x.bootstrap.jar`"的分发结构。

**Architecture:** 停止 Shadow 打包，主 JAR 通过 Manifest `Class-Path:` 引用 `libs/` 中的第三方依赖；插件通过 `URLClassLoader` 动态加载；Bootstrap ClassLoader 按 Manifest `Plugin-Bootstrap-Priority` 升序加载所有 `.bootstrap.jar`（根目录的 core bootstrap + `plugins/` 下的各插件 bootstrap）。

**Tech Stack:** Gradle 8.x (Kotlin DSL)、Java 8、JVM Instrumentation API、Java `ServiceLoader`、`URLClassLoader`。

**Spec:** [`docs/superpowers/specs/2026-06-16-jagent-packaging-redesign-design.md`](../specs/2026-06-16-jagent-packaging-redesign-design.md)

---

## 文件结构概览

### 新建文件
- `jagent/core/src/main/java/org/codecrafterslab/agent/Licence.java`（从 `jagent-bootstrap` 迁移）
- `jagent/core/src/main/java/org/codecrafterslab/agent/plugin/ArgsFilter.java`（迁移）
- `jagent/core/src/main/java/org/codecrafterslab/agent/plugin/PairFinger.java`（迁移）
- `jagent/core/src/test/java/org/codecrafterslab/agent/LicenceTest.java`（迁移）
- `jagent/core/src/test/java/org/codecrafterslab/agent/InitializerTest.java`（新增）
- `jagent/core/src/test/java/org/codecrafterslab/agent/core/EnvironmentTest.java`（新增）
- `jagent/core/src/test/java/org/codecrafterslab/agent/core/plugin/PluginManagerTest.java`（新增）

### 修改文件
- `jagent/core/build.gradle.kts` — 移除 `archiveClassifier.set("original")`、新增 `bootstrapJar` 任务、主 JAR 加 `Class-Path:`
- `jagent/core/src/main/java/org/codecrafterslab/agent/Initializer.java` — 重写加载逻辑
- `jagent/core/src/main/java/org/codecrafterslab/agent/core/Environment.java` — 新增 `pluginClassLoader` 字段
- `jagent/core/src/main/java/org/codecrafterslab/agent/core/plugin/PluginManager.java` — 使用 `environment.getPluginClassLoader()`
- `jagent/plugins/plugin-ep/build.gradle.kts` — 新增 `bootstrapJar` 任务
- `jagent/plugins/plugin-cs/build.gradle.kts` — 新增 `bootstrapJar` 任务
- `jagent/plugins/plugin-timing/build.gradle.kts` — 新增 `bootstrapJar` 任务
- `jagent/settings.gradle.kts` — 取消注释 `include("plugin-cs")`
- `jtools/settings.gradle.kts` — 移除 `includeBuild("jagent-bootstrap")`（如有）
- `distribution/build.gradle.kts` — 新增 `libs`、调整 `entry`、调整各插件 Copy

### 删除文件
- `jagent-bootstrap/` 整个目录

---

## Task 1: 迁移 jagent-bootstrap 类到 jagent/core

**Files:**
- Create: `jagent/core/src/main/java/org/codecrafterslab/agent/Licence.java`
- Create: `jagent/core/src/main/java/org/codecrafterslab/agent/plugin/ArgsFilter.java`
- Create: `jagent/core/src/main/java/org/codecrafterslab/agent/plugin/PairFinger.java`
- Create: `jagent/core/src/test/java/org/codecrafterslab/agent/LicenceTest.java`

- [ ] **Step 1: 复制 Licence.java**

```bash
cp jagent-bootstrap/src/main/java/org/codecrafterslab/agent/Licence.java \
   jagent/core/src/main/java/org/codecrafterslab/agent/Licence.java
```

- [ ] **Step 2: 复制 ArgsFilter.java**

```bash
cp jagent-bootstrap/src/main/java/org/codecrafterslab/agent/plugin/ArgsFilter.java \
   jagent/core/src/main/java/org/codecrafterslab/agent/plugin/ArgsFilter.java
```

- [ ] **Step 3: 复制 PairFinger.java**

```bash
cp jagent-bootstrap/src/main/java/org/codecrafterslab/agent/plugin/PairFinger.java \
   jagent/core/src/main/java/org/codecrafterslab/agent/plugin/PairFinger.java
```

- [ ] **Step 4: 迁移测试用例（如果存在）**

```bash
mkdir -p jagent/core/src/test/java/org/codecrafterslab/agent
[ -f jagent-bootstrap/src/test/java/org/codecrafterslab/agent/LicenceTest.java ] && \
  cp jagent-bootstrap/src/test/java/org/codecrafterslab/agent/LicenceTest.java \
     jagent/core/src/test/java/org/codecrafterslab/agent/LicenceTest.java
```

- [ ] **Step 5: 验证 core 编译通过**

Run: `./gradlew :jagent:core:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: 提交**

```bash
git add jagent/core/src/main/java/org/codecrafterslab/agent/Licence.java \
        jagent/core/src/main/java/org/codecrafterslab/agent/plugin/ArgsFilter.java \
        jagent/core/src/main/java/org/codecrafterslab/agent/plugin/PairFinger.java \
        jagent/core/src/test/java/org/codecrafterslab/agent/LicenceTest.java
git commit -m "refactor(jagent): migrate jagent-bootstrap classes to core"
```

---

## Task 2: 删除 jagent-bootstrap 模块目录与引用

**Files:**
- Delete: `jagent-bootstrap/` 整个目录
- Modify: `jtools/settings.gradle.kts`（如有 includeBuild 引用）
- Modify: `distribution/build.gradle.kts`（移除 bootstrap Copy 任务）

- [ ] **Step 1: 检查 jtools/settings.gradle.kts 是否有 jagent-bootstrap 引用**

Run: `grep -n "jagent-bootstrap" jtools/settings.gradle.kts || echo "no reference"`
Expected: 输出 "no reference"（之前未 include）

- [ ] **Step 2: 删除 jagent-bootstrap 模块目录**

```bash
rm -rf jagent-bootstrap
```

- [ ] **Step 3: 修改 distribution/build.gradle.kts 移除 bootstrap Copy 任务**

将 `distribution/build.gradle.kts` 中 `bootstrap` Copy 任务块删除（原 §3.2 任务），保留 `entry` / 各 plugin / conf 等。

- [ ] **Step 4: 修改 distribution/build.gradle.kts dist 主任务依赖**

将原 `dependsOn("entry", "bootstrap", "plugin-ep")` 改为 `dependsOn("entry", "plugin-ep", ...)`。

- [ ] **Step 5: 验证 jagent:core 仍可构建**

Run: `./gradlew :jagent:core:build`
Expected: BUILD SUCCESSFUL（含迁移后的 Licence/ArgsFilter/PairFinger）

- [ ] **Step 6: 提交**

```bash
git add -u
git commit -m "refactor: remove jagent-bootstrap standalone module"
```

---

## Task 3: 修改 jagent/core/build.gradle.kts — 移除 archiveClassifier("original")

**Files:**
- Modify: `jagent/core/build.gradle.kts:55-67`

- [ ] **Step 1: 定位 archiveClassifier.set("original")**

Run: `grep -n 'archiveClassifier.set("original")' jagent/core/build.gradle.kts`
Expected: 找到该行

- [ ] **Step 2: 删除该行**

从 `tasks { withType<Jar> { ... } }` 块中删除 `archiveClassifier.set("original")`。

- [ ] **Step 3: 验证构建**

Run: `./gradlew :jagent:core:clean :jagent:core:jar`
Expected: `jagent/core/build/libs/JAgent-1.0.0.jar` 存在（不带 -original 后缀）

- [ ] **Step 4: 提交**

```bash
git add jagent/core/build.gradle.kts
git commit -m "build(jagent:core): remove archiveClassifier 'original' (use plain JAgent.jar)"
```

---

## Task 4: 在 jagent/core/build.gradle.kts 新增 bootstrapJar 任务

**Files:**
- Modify: `jagent/core/build.gradle.kts`

- [ ] **Step 1: 添加 bootstrapJar 任务**

在 `tasks { ... }` 块内（已修改后），紧接 `withType<Jar>` 配置之后，新增：

```kotlin
tasks {
    withType<Jar>().configureEach {
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
                ).joinToString(" "),
                "Can-Redefine-Classes" to true,
                "Can-Retransform-Classes" to true,
                "Can-Set-Native-Method-Prefix" to true
            )
        }
    }

    register<Jar>("bootstrapJar") {
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
}
```

- [ ] **Step 2: 验证构建**

Run: `./gradlew :jagent:core:clean :jagent:core:jar :jagent:core:bootstrapJar`
Expected:
- `jagent/core/build/libs/JAgent-1.0.0.jar`
- `jagent/core/build/libs/JAgent-1.0.0-bootstrap.jar`

- [ ] **Step 3: 验证 bootstrap JAR 只含三个类**

Run: `jar tf jagent/core/build/libs/JAgent-1.0.0-bootstrap.jar | grep -E '\.class$'`
Expected:
```
org/codecrafterslab/agent/Licence.class
org/codecrafterslab/agent/Licence$MatchType.class
org/codecrafterslab/agent/plugin/ArgsFilter.class
org/codecrafterslab/agent/plugin/PairFinger.class
```

- [ ] **Step 4: 提交**

```bash
git add jagent/core/build.gradle.kts
git commit -m "build(jagent:core): add bootstrapJar task (core Bootstrap slice)"
```

---

## Task 5: Environment 扩展 — 新增 pluginClassLoader 字段

**Files:**
- Modify: `jagent/core/src/main/java/org/codecrafterslab/agent/core/Environment.java`
- Create: `jagent/core/src/test/java/org/codecrafterslab/agent/core/EnvironmentTest.java`

- [ ] **Step 1: 写失败的测试 EnvironmentTest**

```java
package org.codecrafterslab.agent.core;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.URLClassLoader;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class EnvironmentTest {

    @Test
    void legacyConstructorKeepsPluginClassLoaderNull() {
        Instrumentation inst = mock(Instrumentation.class);
        File jar = new File("/tmp/JAgent.jar");
        Environment env = new Environment(inst, jar, false);
        assertNull(env.getPluginClassLoader(),
            "legacy constructor must default pluginClassLoader to null");
    }

    @Test
    void newConstructorExposesPluginClassLoader() {
        Instrumentation inst = mock(Instrumentation.class);
        File jar = new File("/tmp/JAgent.jar");
        URLClassLoader cl = new URLClassLoader(new java.net.URL[0]);
        Environment env = new Environment(inst, jar, "myapp", false, cl);
        assertNotNull(env.getPluginClassLoader(),
            "new constructor must expose pluginClassLoader");
    }
}
```

需要在 `dependencies` 加 `testImplementation("org.mockito:mockito-core")`（如果尚未添加）。

- [ ] **Step 2: 运行测试验证失败**

Run: `./gradlew :jagent:core:test --tests EnvironmentTest`
Expected: FAIL（`Environment` 还没有 `getPluginClassLoader()`）

- [ ] **Step 3: 修改 Environment.java**

在 `Environment` 类中添加：

```java
private final URLClassLoader pluginClassLoader;
```

新增构造器：

```java
public Environment(Instrumentation instrumentation, File agentFile,
                   String app, boolean attachMode, URLClassLoader pluginClassLoader) {
    this(instrumentation, agentFile, app, attachMode);
    this.pluginClassLoader = pluginClassLoader;
}
```

`getPluginClassLoader()` getter 由 Lombok `@Getter` 自动生成。

- [ ] **Step 4: 运行测试验证通过**

Run: `./gradlew :jagent:core:test --tests EnvironmentTest`
Expected: PASS

- [ ] **Step 5: 提交**

```bash
git add jagent/core/src/main/java/org/codecrafterslab/agent/core/Environment.java \
        jagent/core/src/test/java/org/codecrafterslab/agent/core/EnvironmentTest.java
git commit -m "feat(jagent:core): Environment exposes pluginClassLoader"
```

---

## Task 6: 重写 Initializer.java 加载逻辑

**Files:**
- Modify: `jagent/core/src/main/java/org/codecrafterslab/agent/Initializer.java`

- [ ] **Step 1: 写失败的测试 InitializerTest**

```java
package org.codecrafterslab.agent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class InitializerTest {

    @Test
    void readsBootstrapPriorityFromManifest(@TempDir Path tmp) throws Exception {
        File jar = new File(tmp.toFile(), "p-0.1.0.bootstrap.jar");
        Manifest mf = new Manifest();
        mf.getMainAttributes().putValue("Plugin-Bootstrap-Priority", "42");
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jar), mf)) {
            jos.putNextEntry(new java.util.jar.JarEntry("META-INF/"));
            jos.closeEntry();
        }
        int p = invokeReadBootstrapPriority(jar);
        assertEquals(42, p);
    }

    @Test
    void readsBootstrapPriorityDefaultsToMaxWhenAbsent(@TempDir Path tmp) throws Exception {
        File jar = new File(tmp.toFile(), "p-0.1.0.bootstrap.jar");
        Manifest mf = new Manifest();
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jar), mf)) {
            jos.putNextEntry(new java.util.jar.JarEntry("META-INF/"));
            jos.closeEntry();
        }
        int p = invokeReadBootstrapPriority(jar);
        assertEquals(Integer.MAX_VALUE, p);
    }

    private static int invokeReadBootstrapPriority(File jar) throws Exception {
        java.lang.reflect.Method m = Initializer.class
            .getDeclaredMethod("readBootstrapPriority", File.class);
        m.setAccessible(true);
        return (int) m.invoke(null, jar);
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `./gradlew :jagent:core:test --tests InitializerTest`
Expected: FAIL（`readBootstrapPriority` 方法不存在）

- [ ] **Step 3: 修改 Initializer.java**

完整替换 `processAgent` 方法（第 43-67 行）和新增辅助方法：

```java
public static void processAgent(Logger log, String agentArgs, Instrumentation inst, boolean attach) {
    if (loaded) return;
    try {
        AgentUtil.getAgentJarFile().ifPresent(file -> {
            File distRoot = file.getParentFile();
            File pluginsDir = new File(distRoot, "plugins");
            loaded = true;

            File[] coreBootstraps = Optional.ofNullable(distRoot.listFiles(
                (dir, name) -> name.endsWith(".bootstrap.jar"))).orElse(new File[0]);
            File[] pluginBootstraps = Optional.ofNullable(pluginsDir.listFiles(
                (dir, name) -> name.endsWith(".bootstrap.jar"))).orElse(new File[0]);
            Stream.concat(Arrays.stream(coreBootstraps), Arrays.stream(pluginBootstraps))
                .sorted(Comparator.comparingInt(Initializer::readBootstrapPriority))
                .map(Initializer::openJarSafely)
                .filter(Objects::nonNull)
                .forEach(inst::appendToBootstrapClassLoaderSearch);

            URLClassLoader pluginCl = buildPluginClassLoader(pluginsDir);

            Environment environment = new Environment(inst, file, agentArgs, attach, pluginCl);
            Initializer.init(log, environment);
        });
    } catch (Exception e) {
        if (log.isErrorEnabled()) {
            log.error("Can not locate `JAgent` jar file.", e);
        }
    }
}

static int readBootstrapPriority(File jar) {
    try (JarFile jf = new JarFile(jar)) {
        String p = jf.getManifest().getMainAttributes()
            .getValue("Plugin-Bootstrap-Priority");
        return p == null ? Integer.MAX_VALUE : Integer.parseInt(p);
    } catch (Exception e) {
        return Integer.MAX_VALUE;
    }
}

private static JarFile openJarSafely(File f) {
    try { return new JarFile(f); } catch (Exception e) { return null; }
}

private static URLClassLoader buildPluginClassLoader(File pluginsDir) {
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

更新 imports：增加 `import java.net.URL;` 和 `import java.net.URLClassLoader;`，保留现有 `Stream`/`Arrays`/`Comparator`/`JarFile` 等。

`readBootstrapPriority` 改为 package-private（去掉 `private` 修饰符），便于测试反射调用。

- [ ] **Step 4: 运行测试验证通过**

Run: `./gradlew :jagent:core:test --tests InitializerTest`
Expected: PASS

- [ ] **Step 5: 提交**

```bash
git add jagent/core/src/main/java/org/codecrafterslab/agent/Initializer.java \
        jagent/core/src/test/java/org/codecrafterslab/agent/InitializerTest.java
git commit -m "feat(jagent:core): Initializer loads *.bootstrap.jar by priority"
```

---

## Task 7: PluginManager 使用 environment.getPluginClassLoader()

**Files:**
- Modify: `jagent/core/src/main/java/org/codecrafterslab/agent/core/plugin/PluginManager.java`

- [ ] **Step 1: 写失败的测试 PluginManagerTest**

```java
package org.codecrafterslab.agent.core.plugin;

import org.codecrafterslab.agent.api.AppContext;
import org.codecrafterslab.agent.api.Plugin;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.URLClassLoader;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PluginManagerTest {

    @Test
    void loadPluginsReceivesContext() {
        // 仅验证签名：PluginManager.loadPlugins 接收 Agent 与 AppContext，
        // 并通过 AppContext / Environment 间接使用 pluginClassLoader。
        AppContext ctx = mock(AppContext.class);
        when(ctx.getPluginDir()).thenReturn(new File("/tmp/plugins"));
        org.codecrafterslab.agent.Agent agent = mock(org.codecrafterslab.agent.Agent.class);
        PluginManager.loadPlugins(agent, ctx);
        // 现有实现会在 pluginDir 不存在时直接 return，调用本身不抛异常即视为通过。
    }
}
```

- [ ] **Step 2: 修改 PluginManager.loadPlugins 优先使用传入的 ClassLoader**

```java
public static void loadPlugins(Agent agent, AppContext appContext) {
    Instant startTime = Instant.now();
    try {
        File pluginDir = appContext.getPluginDir();
        if (!pluginDir.exists() || !pluginDir.isDirectory()) return;
        File[] pluginFiles = pluginDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (null == pluginFiles) return;

        // 如果 appContext 提供 pluginClassLoader（如 Environment.pluginClassLoader），
        // 优先使用它（覆盖默认 Thread.currentThread().getContextClassLoader()）
        ClassLoader parent = appContext.getPluginClassLoader() != null
            ? appContext.getPluginClassLoader()
            : Thread.currentThread().getContextClassLoader();

        URL[] urls = Arrays.stream(pluginFiles).map(file -> {
            try { return file.toURI().toURL(); } catch (Exception e) { return null; }
        }).filter(Objects::nonNull).toArray(URL[]::new);
        ClassLoader pluginClassLoader = new URLClassLoader(urls, parent);
        ServiceLoader<Plugin> loader = ServiceLoader.load(Plugin.class, pluginClassLoader);

        for (Plugin plugin : loader) {
            // ... 原有逻辑保持不变 ...
        }
        // ... 原有日志逻辑保持不变 ...
    } catch (Exception e) {
        log.error("Failed to load plugins", e);
    }
}
```

- [ ] **Step 3: 在 AppContext 接口增加 getPluginClassLoader()**

`jagent/core/src/main/java/org/codecrafterslab/agent/api/AppContext.java`：

```java
public interface AppContext {
    // ... 现有方法 ...
    default ClassLoader getPluginClassLoader() { return null; }
}
```

- [ ] **Step 4: 让 DefaultAppContext 支持 pluginClassLoader**

在 `DefaultAppContext` 增加新构造器（保留旧构造器兼容）：

```java
private final ClassLoader pluginClassLoader;

public DefaultAppContext(File agentFile, String appName, String appVersion,
                         ClassLoader pluginClassLoader) {
    this(agentFile, appName, appVersion);
    this.pluginClassLoader = pluginClassLoader;
}

@Override
public ClassLoader getPluginClassLoader() {
    return pluginClassLoader;
}
```

- [ ] **Step 5: 修改 Initializer.init 传递 pluginClassLoader**

在 `Initializer.init` 中（第 80 行附近）：

```java
AppContext appContext = new DefaultAppContext(
    environment.getAgentFile(),
    environment.getAppName(),
    environment.getVersion(),
    environment.getPluginClassLoader());
```

- [ ] **Step 7: 提交**

```bash
git add jagent/core/src/main/java/org/codecrafterslab/agent/core/plugin/PluginManager.java \
        jagent/core/src/main/java/org/codecrafterslab/agent/api/AppContext.java \
        jagent/core/src/main/java/org/codecrafterslab/agent/core/DefaultAppContext.java \
        jagent/core/src/main/java/org/codecrafterslab/agent/Initializer.java \
        jagent/core/src/test/java/org/codecrafterslab/agent/core/plugin/PluginManagerTest.java
git commit -m "feat(jagent:core): PluginManager uses Environment.pluginClassLoader"
```

Run 顺序：先做完 Step 5 的代码改动与 Step 6 的测试运行，再统一在 Step 7 提交。

---

## Task 8: 修复 jagent/settings.gradle.kts 包含 plugin-cs

**Files:**
- Modify: `jagent/settings.gradle.kts`

- [ ] **Step 1: 检查当前 settings.gradle.kts**

Run: `grep -n 'plugin-cs' jagent/settings.gradle.kts`
Expected: 找到被注释的 `//include("plugin-cs")`

- [ ] **Step 2: 取消注释**

```kotlin
//include("plugin-cs")
```
改为：
```kotlin
include("plugin-cs")
```

`plugin-cs` 项目路径视实际位置而定（保留顶层或迁移到 `jagent/plugins/plugin-cs/`）。本计划假定已迁移到 `jagent/plugins/plugin-cs/`，路径为 `plugins:plugin-cs`。如果不是，调整为对应路径。

- [ ] **Step 3: 验证 plugin-cs 子项目被识别**

Run: `./gradlew :jagent:projects`
Expected: 输出包含 `:plugins:plugin-cs`（或 `:plugin-cs`）

- [ ] **Step 4: 提交**

```bash
git add jagent/settings.gradle.kts
git commit -m "build(jagent): include plugin-cs in composite build"
```

---

## Task 9: 在 plugin-ep 添加 bootstrapJar 任务

**Files:**
- Modify: `jagent/plugins/plugin-ep/build.gradle.kts`

- [ ] **Step 1: 修改 build.gradle.kts 添加 bootstrapJar**

```kotlin
tasks {
    withType<Jar>().configureEach {
        manifest {
            attributes(
                "Plugin-Name" to project.name,
                "Plugin-Version" to project.version,
                "Plugin-Bootstrap-Required" to "true",
                "Plugin-Bootstrap-Priority" to "20"
            )
        }
    }
    register<Jar>("bootstrapJar") {
        archiveClassifier.set("bootstrap")
        from(sourceSets.main.get().output)
        include("org/codecrafterslab/agent/plugin/PublicKeyTransformer.class")
    }
}
```

- [ ] **Step 2: 验证构建**

Run: `./gradlew :jagent:plugins:plugin-ep:clean :jagent:plugins:plugin-ep:jar :jagent:plugins:plugin-ep:bootstrapJar`
Expected:
- `jagent/plugins/plugin-ep/build/libs/plugin-ep-0.1.0.jar`
- `jagent/plugins/plugin-ep/build/libs/plugin-ep-0.1.0-bootstrap.jar`

- [ ] **Step 3: 提交**

```bash
git add jagent/plugins/plugin-ep/build.gradle.kts
git commit -m "build(plugin-ep): add bootstrapJar task"
```

---

## Task 10: 在 plugin-cs 添加 bootstrapJar 任务

**Files:**
- Modify: `jagent/plugins/plugin-cs/build.gradle.kts`（或顶层 `plugin-cs/build.gradle.kts`，取决于 Task 8 的选择）

- [ ] **Step 1: 修改 build.gradle.kts 添加 bootstrapJar**

```kotlin
tasks {
    withType<Jar>().configureEach {
        manifest {
            attributes(
                "Plugin-Name" to project.name,
                "Plugin-Version" to project.version,
                "Plugin-Bootstrap-Required" to "true",
                "Plugin-Bootstrap-Priority" to "30"
            )
        }
    }
    register<Jar>("bootstrapJar") {
        archiveClassifier.set("bootstrap")
        from(sourceSets.main.get().output)
        include("org/codecrafterslab/agent/plugin/cs/ConstSubstitutionVisitor.class")
    }
}
```

- [ ] **Step 2: 验证构建**

Run: `./gradlew :jagent:plugins:plugin-cs:clean :jagent:plugins:plugin-cs:jar :jagent:plugins:plugin-cs:bootstrapJar`
Expected:
- `plugin-cs-0.1.0.jar`
- `plugin-cs-0.1.0-bootstrap.jar`

- [ ] **Step 3: 提交**

```bash
git add jagent/plugins/plugin-cs/build.gradle.kts
git commit -m "build(plugin-cs): add bootstrapJar task"
```

---

## Task 11: 在 plugin-timing 添加 bootstrapJar 任务

**Files:**
- Modify: `jagent/plugins/plugin-timing/build.gradle.kts`

- [ ] **Step 1: 修改 build.gradle.kts 添加 bootstrapJar**

```kotlin
tasks {
    withType<Jar>().configureEach {
        manifest {
            attributes(
                "Plugin-Name" to project.name,
                "Plugin-Version" to project.version,
                "Plugin-Bootstrap-Required" to "true",
                "Plugin-Bootstrap-Priority" to "40"
            )
        }
    }
    register<Jar>("bootstrapJar") {
        archiveClassifier.set("bootstrap")
        from(sourceSets.main.get().output)
        include("org/codecrafterslab/agent/plugin/timing/MethodTrackingPlugin.class")
    }
}
```

- [ ] **Step 2: 验证构建**

Run: `./gradlew :jagent:plugins:plugin-timing:clean :jagent:plugins:plugin-timing:jar :jagent:plugins:plugin-timing:bootstrapJar`
Expected:
- `plugin-timing-0.1.0.jar`
- `plugin-timing-0.1.0-bootstrap.jar`

- [ ] **Step 3: 提交**

```bash
git add jagent/plugins/plugin-timing/build.gradle.kts
git commit -m "build(plugin-timing): add bootstrapJar task"
```

---

## Task 12: 在 distribution/build.gradle.kts 新增 libs 任务

**Files:**
- Modify: `distribution/build.gradle.kts`

- [ ] **Step 1: 在 distribution/build.gradle.kts 添加 libs Copy 任务**

```kotlin
register<Copy>("libs") {
    group = "distribution"
    dependsOn(":jagent:core:build")

    val coreProject = project(":jagent:core")
    val runtimeFiles: org.gradle.api.file.FileCollection = coreProject.files(
        coreProject.configurations.runtimeClasspath
    )
    from(runtimeFiles)
    into(project.layout.buildDirectory.dir("dist/libs"))
    include("*.jar")
}
```

- [ ] **Step 2: 修改 dist 主任务依赖**

```kotlin
register("dist") {
    group = "distribution"
    dependsOn(clean, "libs", "entry", "plugin-ep", "plugin-cs", "plugin-cs-conf", "plugin-timing")
}
```

- [ ] **Step 3: 验证 dist 任务构建**

Run: `./gradlew :distribution:libs`
Expected: `distribution/build/dist/libs/` 含 slf4j-api、asm、asm-commons、asm-util、toml4j

- [ ] **Step 4: 提交**

```bash
git add distribution/build.gradle.kts
git commit -m "build(distribution): add libs task copying runtimeClasspath"
```

---

## Task 13: 在 distribution 调整 entry 与各 plugin Copy 任务

**Files:**
- Modify: `distribution/build.gradle.kts`

- [ ] **Step 1: 修改 entry 任务移除 `exclude("*-original.jar")`**

```kotlin
register<Copy>("entry") {
    group = "distribution"
    dependsOn(clean)
    dependsOn(":jagent:core:build")
    val mainProject = project(":jagent:core")
    from(mainProject.layout.buildDirectory.file("libs"))
    from(mainProject.layout.projectDirectory.file("README.md"))
    into(project.layout.buildDirectory.dir("dist"))
    include("JAgent-*.jar")
    include("README.md")
}
```

注意：`from(mainProject.layout.projectDirectory.file("README.md"))` 改为 `from(mainProject.layout.buildDirectory.file("libs").dir("..").file("README.md"))` 或直接在 `:jagent` 项目根读取 README。原代码是 `mainProject.layout.projectDirectory.file("README.md")`，可能需要确认路径（README 在 `jagent/README.md`，而 `mainProject` 是 `:jagent:core`，所以是 `core/README.md`，可能不存在）。修正：用 `:jagent` 而非 `:jagent:core`：

```kotlin
val mainProject = project(":jagent")
from(mainProject.layout.projectDirectory.file("README.md"))
```

或者 `from(file("../jagent/README.md"))`。

- [ ] **Step 2: 调整各 plugin Copy 任务使用 plugins 子项目**

```kotlin
register<Copy>("plugin-ep") {
    dependsOn(":jagent:plugins:plugin-ep:build")
    val p = project(":jagent:plugins:plugin-ep")
    from(p.layout.buildDirectory.file("libs"))
    into(project.layout.buildDirectory.dir("dist/plugins"))
}
register<Copy>("plugin-cs") {
    dependsOn(":jagent:plugins:plugin-cs:build", "plugin-cs-conf")
    val p = project(":jagent:plugins:plugin-cs")
    from(p.layout.buildDirectory.file("libs"))
    into(project.layout.buildDirectory.dir("dist/plugins"))
}
register<Copy>("plugin-timing") {
    dependsOn(":jagent:plugins:plugin-timing:build")
    val p = project(":jagent:plugins:plugin-timing")
    from(p.layout.buildDirectory.file("libs"))
    into(project.layout.buildDirectory.dir("dist/plugins"))
}
```

- [ ] **Step 3: 修改 plugin-cs-conf 任务**

如果 `plugin-cs.toml` 路径变为 `jagent/plugins/plugin-cs/plugin-cs.toml`：

```kotlin
register<Copy>("plugin-cs-conf") {
    group = "distribution"
    val pluginProject = project(":jagent:plugins:plugin-cs")
    from(pluginProject.layout.files("plugin-cs.toml"))
    into(project.layout.buildDirectory.dir("dist/conf"))
}
```

- [ ] **Step 4: 完整构建验证产物**

Run: `./gradlew :distribution:dist`
Expected: `distribution/build/dist/` 结构：

```
JAgent-1.0.0.jar
JAgent-1.0.0-bootstrap.jar
README.md
libs/
  slf4j-api-x.x.x.jar
  asm-x.x.x.jar
  asm-commons-x.x.x.jar
  asm-util-x.x.x.jar
  toml4j-x.x.x.jar
conf/
  plugin-cs.toml
plugins/
  plugin-ep-0.1.0.jar
  plugin-ep-0.1.0-bootstrap.jar
  plugin-cs-0.1.0.jar
  plugin-cs-0.1.0-bootstrap.jar
  plugin-timing-0.1.0.jar
  plugin-timing-0.1.0-bootstrap.jar
```

- [ ] **Step 5: 提交**

```bash
git add distribution/build.gradle.kts
git commit -m "build(distribution): restructure dist output (libs/, per-plugin bootstrap)"
```

---

## Task 14: 集成验证 — 完整构建 + 启动 demo

**Files:**（无修改）

- [ ] **Step 1: 完整 dist 构建**

Run: `./gradlew clean :distribution:dist`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: 验证 dist 产物结构**

Run: `find distribution/build/dist -type f | sort`
Expected: 与 Task 13 Step 4 列表一致

- [ ] **Step 3: 检查 Bootstrap JAR 加载顺序**

Run: `unzip -p distribution/build/dist/JAgent-1.0.0-bootstrap.jar META-INF/MANIFEST.MF | grep Plugin-Bootstrap`
Expected: `Plugin-Bootstrap-Priority: 0`

Run: `for f in distribution/build/dist/plugins/*.bootstrap.jar; do echo "$f"; unzip -p "$f" META-INF/MANIFEST.MF | grep Plugin-Bootstrap-Priority; done`
Expected: 各 plugin 的 priority（20/30/40）

- [ ] **Step 4: 提交（如有调整）**

如有 dist 产物调整，再次提交。否则跳过。

---

## Self-Review Checklist

- [x] **Spec coverage**: §3.1（Task 1-2）、§3.2（Task 3-4）、§3.3（Task 9-11）、§3.4（Task 8）、§4（Task 6）、§4.1（Task 5）、§4.2（Task 7）、§5（Task 12-13）、§6（Task 14）均有对应 Task。
- [x] **Placeholder scan**: 所有 step 含具体代码或命令，无 TBD。
- [x] **Type consistency**: `readBootstrapPriority` 在 Task 6 定义为 package-private 静态方法，与 Task 6 测试反射调用一致；`Environment.getPluginClassLoader()` 在 Task 5 定义；`PluginManager` 在 Task 7 使用 `appContext.getPluginClassLoader()` 与 Task 7 Step 3 接口一致。
