import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("ccl.lib")
    id("com.gradleup.shadow") version "9.3.1"
}

group = "org.codecrafterslab.agent"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    api("org.ow2.asm:asm-commons:9.9.1")
//    implementation("org.ow2.asm:asm-util:9.9.1")

    compileOnly(files("/Users/wuyujie/Library/Java/JavaVirtualMachines/corretto-1.8.0_482/Contents/Home/lib/tools.jar"))

    testImplementation("org.ow2.asm:asm-test:9.9.1")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

}

tasks.test {
    useJUnitPlatform()
}

val manifestAttr = mapOf(
    "Implementation-Title" to "Java Agent Proxy",
    "Implementation-Version" to project.version,
    "Built-By" to "coffee377",
    "Built-Date" to LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm.ss.SSS")),
    "Built-Jdk" to System.getProperty("java.version"),
    "Built-Gradle" to gradle.gradleVersion,
    "Agent-class" to "com.janetfilter.core.Launcher",
    "Premain-Class" to "com.janetfilter.core.Launcher",
    "Main-class" to "com.janetfilter.core.Launcher",
    "Can-Redefine-Classes" to true,
    "Can-Retransform-Classes" to true,
    "Can-Set-Native-Method-Prefix" to true
)

tasks {
    withType<Jar> {
        archiveBaseName.set("JAgent")
        manifest {
            attributes(manifestAttr)
        }
        archiveClassifier.set("original")
    }

    shadowJar {
        archiveBaseName.set("JAgent")
        archiveClassifier.set("")
    }
}
