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

var java8: String? = System.getenv("JAVA8_HOME")
if (java8 == null) {
    java8 = "/Users/wuyujie/Library/Java/JavaVirtualMachines/corretto-1.8.0_482/Contents/Home/bin"
}

dependencies {
    api(libs.slf4j.api)
    api(libs.asm.commons)
    implementation(libs.asm.util)

    compileOnly(files("${java8}/../lib/tools.jar"))

    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.asm.test)

    annotationProcessor(libs.lombok)
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
    "Agent-class" to "org.codecrafterslab.agent.Launcher",
    "Premain-Class" to "org.codecrafterslab.agent.Launcher",
    "Main-class" to "org.codecrafterslab.agent.Usage",
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
