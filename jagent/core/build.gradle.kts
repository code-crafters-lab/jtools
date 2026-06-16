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
    api(libs.asm.commons) {
        exclude(group = "org.ow2.asm", module = "asm-tree")
    }
    api(libs.asm.util)
    implementation(libs.toml4j)

    compileOnly(files("${java8}/../lib/tools.jar"))

    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.asm.test)
    testImplementation("org.mockito:mockito-core:5.21.0")

    annotationProcessor(libs.lombok)
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    withType<Jar>().configureEach {
        archiveBaseName.set("JAgent")
    }

    named<Jar>("jar") {
        manifest {
            attributes(
                "Implementation-Title" to "Java Agent Proxy",
                "Implementation-Version" to project.version,
                "Built-By" to "coffee377",
                "Built-Date" to LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm.ss.SSS")),
                "Built-Jdk" to System.getProperty("java.version"),
                "Built-Gradle" to gradle.gradleVersion,
                "Premain-Class" to "org.codecrafterslab.agent.Launcher",
                "Agent-Class" to "org.codecrafterslab.agent.Launcher",
                "Main-Class" to "org.codecrafterslab.agent.Usage",
                "Class-Path" to listOf(
                    "libs/slf4j-api-${libs.versions.slf4j.api.get()}.jar",
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
        include("org/codecrafterslab/agent/Licence*")
        include("org/codecrafterslab/agent/plugin/ArgsFilter*")
        include("org/codecrafterslab/agent/plugin/PairFinger*")
        manifest {
            attributes(
                "Plugin-Name" to "jagent-core-bootstrap",
                "Plugin-Bootstrap-Required" to "true",
                "Plugin-Bootstrap-Priority" to "0"
            )
        }
    }

    shadowJar {
        archiveBaseName.set("JAgent")
        archiveClassifier.set("")
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri(project.layout.buildDirectory.dir("repos"))
        }
    }
}
