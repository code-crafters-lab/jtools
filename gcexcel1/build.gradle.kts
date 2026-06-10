plugins {
    id("ccl.lib")
    application
}

group = "org.codecrafterslab"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("ch.qos.logback:logback-classic:1.5.27")
    implementation("com.grapecitysoft.documents:gcexcel:9.1.1")
    implementation("com.google.code.gson:gson:2.12.1")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    compileOnly(project(":jagent-bootstrap"))

    annotationProcessor("org.projectlombok:lombok:1.18.42")
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    withType<JavaCompile> {
        options.release.set(17)
    }
}

val agent: String =
    project(":distribution").layout.buildDirectory.file("dist/JAgent-1.0.0.jar").get().asFile.absolutePath


/* java agent */
application {
    mainClass.set("GCDemo")
    val args = mutableListOf("-Dfile.encoding=UTF-8")
//    args.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005")
//    args.add("-javaagent:${agent}")
    applicationDefaultJvmArgs = args
}

tasks {

    register<JavaExec>("gcexcel") {
        description = "运行 GCExcel 示例"
        dependsOn(":distribution:dist")
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
            "-javaagent:${agent}",
            "-Dclass.out.dir=${project.layout.buildDirectory.file("code").get().asFile.absolutePath}",
        )

    }

    register<JavaExec>("gcexcel-debug") {
        description = "调试运行 GCExcel 示例"
        dependsOn(":distribution:dist", "license-fake")
        group = "demo"
        mainClass.set("GCDemo")
        classpath = sourceSets.main.get().runtimeClasspath

        jvmArgs = listOf(
            "-Dfile.encoding=UTF-8",
            "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005",
            "-javaagent:${agent}",
            "-Dclass.out.dir=${project.layout.buildDirectory.file("code").get().asFile.absolutePath}",
        )
    }
}
