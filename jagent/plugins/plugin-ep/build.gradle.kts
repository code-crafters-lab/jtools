plugins {
    id("ccl.lib")
}

group = "org.codecrafterslab.agent"

dependencies {
    compileOnly(project(":core"))
    // testImplementation(libs.junit.jupiter)
    // testRuntimeOnly(libs.junit.platform.launcher)
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
            attributes["Plugin-Bootstrap-Required"] = "true"
            attributes["Plugin-Bootstrap-Priority"] = "20"
        }
    }
}
