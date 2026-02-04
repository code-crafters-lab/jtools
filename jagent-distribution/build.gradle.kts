plugins {
    id("ccl.lib")
}

group = "org.codecrafterslab"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":jagent"))
    implementation(project(":plugin-modulus"))
}
