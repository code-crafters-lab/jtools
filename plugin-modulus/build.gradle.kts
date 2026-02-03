plugins {
    id("java")
}

group = "org.codecrafterslab"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":JAgent"))
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    withType<Jar> {
        manifest {
            attributes["JANF-Plugin-Entry"] = "GrapeCityPlugin"
        }
    }
}
