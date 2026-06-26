plugins {
    id("ccl.lib")
    application
}

group = "org.codecrafterslab"
version = "1.0.0-SNAPSHOT"

java {
    registerFeature("hack"){
        usingSourceSet(sourceSets.create("hack"))
        disablePublication()
    }
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("ch.qos.logback:logback-classic:1.3.16")
    implementation("com.grapecitysoft.documents:gcexcel:9.0.1")
    implementation("com.google.code.gson:gson:2.12.1")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation(fileTree("../jagent/core/build/libs") {
        include("*bootstrap.jar")
    })

//    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    testImplementation(sourceSets["hack"].output)
}

configurations {
    named("hackImplementation") {
        extendsFrom(configurations.implementation.get())
    }
    named("hackCompileOnly") {
        extendsFrom(configurations.compileOnly.get())
    }
    named("hackAnnotationProcessor") {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    named<JavaCompile>("compileHackJava") {
        options.release.set(17)
    }
    named<JavaCompile>("compileTestJava") {
        options.release.set(17)
    }
}

val agent: String = "/Users/wuyujie/Project/opensource/jtools/jagent/distribution/build/dist/jagent.jar"

/* java agent */
application {
    mainClass.set("GCDemo")
    val args = mutableListOf("-Dfile.encoding=UTF-8")
//    args.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005")
//    args.add("-javaagent:${agent}")
    applicationDefaultJvmArgs = args
}

java {
    toolchain {
//        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks {

    register<JavaExec>("gcexcel") {
        description = "运行 GCExcel 示例"
        group = "demo"
        mainClass.set("GCDemo")
        classpath = sourceSets.main.get().runtimeClasspath

        val v9File = file(
            project.layout.projectDirectory.dir("src/test/resources")
                .file("v9.lic")
        )
        val v9Text = if (v9File.exists()) v9File.readText().trim() else ""
        if (v9Text.isNotEmpty()) {
            environment("GCEXCEL_JAVA_DEPLOY_LICENSE_V9", v9Text)
        }

        // 写法 2：通过 provider 按需获取（更省时，按任务执行时再解析）
//        javaLauncher.set(
//            project.javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(8)) }
//        )

        jvmArgs = listOf(
            "-Dfile.encoding=UTF-8",
            "-javaagent:${agent}",
            "-Dclass.out.dir=${project.layout.buildDirectory.file("code").get().asFile.absolutePath}",
        )

    }

    register<JavaExec>("gcexcel-debug") {
        description = "调试运行 GCExcel 示例"
        group = "demo"
        mainClass.set("GCDemo")
        classpath = sourceSets.main.get().runtimeClasspath

        val v9File = file(
            project.layout.projectDirectory.dir("src/test/resources")
                .file("v9.lic")
        )
        val v9Text = if (v9File.exists()) v9File.readText().trim() else ""
        if (v9Text.isNotEmpty()) {
            environment("GCEXCEL_JAVA_DEPLOY_LICENSE_V9", v9Text)
        }

        jvmArgs = listOf(
            "-Dfile.encoding=UTF-8",
            "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005",
            "-javaagent:${agent}",
            "-Dclass.out.dir=${project.layout.buildDirectory.file("code").get().asFile.absolutePath}",
        )
    }
}
