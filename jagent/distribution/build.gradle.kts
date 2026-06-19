plugins {
    id("ccl.lib")
}

group = "org.codecrafterslab"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation(project(":plugins:plugin-ep"))
    implementation(project(":plugins:plugin-cs"))
    implementation(project(":plugins:plugin-timing"))
}

tasks {
    register<Copy>("libs") {
        group = "distribution"
        dependsOn(":core:build")

        val coreProject = project(":core")
        val runtimeFiles: org.gradle.api.file.FileCollection = coreProject.files(
            coreProject.configurations.runtimeClasspath
        )
        from(runtimeFiles)
        into(project.layout.buildDirectory.dir("dist/libs"))
        include("*.jar")
    }

    register<Copy>("dist") {
        group = "distribution"
        dependsOn(clean, "libs", "entry", "plugin-ep", "plugin-cs", "plugin-cs-conf", "plugin-timing")
    }

    register<Copy>("entry") {
        group = "distribution"
        dependsOn(clean)
        dependsOn(":core:build")
        val mainProject = project(":core")
        from(mainProject.layout.buildDirectory.file("libs"))
        val jagentProject = project(":")
        from(jagentProject.layout.projectDirectory.file("README.md"))
        into(project.layout.buildDirectory.dir("dist"))
    }

    register<Copy>("plugin-ep") {
        group = "distribution"
        dependsOn(":plugins:plugin-ep:build")

        val pluginProject = project(":plugins:plugin-ep")
        from(pluginProject.layout.buildDirectory.file("libs"))
        into(project.layout.buildDirectory.dir("dist/plugins"))
    }

    register<Copy>("plugin-cs") {
        group = "distribution"
        dependsOn(":plugins:plugin-cs:build", "plugin-cs-conf")

        val pluginProject = project(":plugins:plugin-cs")
        from(pluginProject.layout.buildDirectory.file("libs"))
        into(project.layout.buildDirectory.dir("dist/plugins"))
    }

    register<Copy>("plugin-cs-conf") {
        group = "distribution"

        val pluginProject = project(":plugins:plugin-cs")
        from(pluginProject.layout.files("plugin-cs.toml"))
        into(project.layout.buildDirectory.dir("dist/conf"))
    }

    register<Copy>("plugin-timing") {
        group = "distribution"
        dependsOn(":plugins:plugin-timing:build")

        val pluginProject = project(":plugins:plugin-timing")
        from(pluginProject.layout.buildDirectory.file("libs"))
        into(project.layout.buildDirectory.dir("dist/plugins"))
    }

}
