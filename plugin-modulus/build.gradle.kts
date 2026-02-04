plugins {
    id("ccl.lib")
}

group = "org.codecrafterslab"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":jagent"))

    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    withType<Jar> {
        manifest {
            attributes["JANF-Plugin-Entry"] = "ModulusPlugin"
        }
    }
}
