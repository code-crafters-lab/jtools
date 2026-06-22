import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("ccl.lib")
}

group = "org.codecrafterslab.agent"
version = "1.0.0"

var java8: String? = System.getenv("JAVA8_HOME")
if (java8 == null) {
    java8 = "/Users/wuyujie/Library/Java/JavaVirtualMachines/corretto-1.8.0_492/Contents/Home"
}

java {
    // 1. 注册第一个特性：bootstrap
    registerFeature("bootstrap") {
        // 绑定独立源码集（src/bootstrap/java）
        usingSourceSet(sourceSets.create("bootstrap"))
    }
}

dependencies {
    api(libs.slf4j.api)
    api(libs.asm)
    api(libs.asm.tree)
    api(libs.asm.commons)

    compileOnly(libs.toml4j)
    compileOnly(files("${java8}/lib/tools.jar"))

    testImplementation(libs.asm.test)
    testImplementation("org.mockito:mockito-core:5.21.0")

    annotationProcessor(libs.lombok)
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    withType<Jar>().configureEach {
        archiveBaseName.set("jagent")
    }

    named<Jar>("jar") {
        dependsOn("bootstrapJar")
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
                "Can-Redefine-Classes" to true,
                "Can-Retransform-Classes" to true,
                "Can-Set-Native-Method-Prefix" to true
            )

        }
    }

    named<Jar>("bootstrapJar") {
        manifest {
            attributes(
                "Automatic-Module-Name" to "${project.name}-bootstrap",
            )
        }
    }

}

publishing {
    publications {
        withType<MavenPublication>().configureEach {
            suppressPomMetadataWarningsFor("bootstrapApiElements")
            suppressPomMetadataWarningsFor("bootstrapRuntimeElements")
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri(project.layout.buildDirectory.dir("repos"))
        }
    }
}
