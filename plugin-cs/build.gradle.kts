plugins {
    id("ccl.lib")
}

group = "org.codecrafterslab.agent"
version = "0.1.0"
description = "A Const Substitution Plugin for JAgent."

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
            attributes["Plugin-Name"] = project.name
            attributes["Plugin-Author"] = "coffee377"
            attributes["Plugin-Version"] = project.version
            attributes["Plugin-Description"] = project.description
//            attributes["JANF-Plugin-Entry"] = "org.codecrafterslab.agent.plugin.cs.ConstSubstitutionPlugin"
        }
    }
}
