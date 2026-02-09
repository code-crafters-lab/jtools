plugins {
    id("ccl.lib")
}

group = "org.codecrafterslab.agent"
version = "0.1.0"
description = "A jagent-bootstrap JAR."

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":jagent"))

    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    annotationProcessor(libs.lombok)
    annotationProcessor(libs.google.auto.service)
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    withType<Jar> {
        manifest {

        }
    }
}
