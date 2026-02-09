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
    implementation(project(":plugin-cs"))
    implementation(project(":plugin-timing"))
}

tasks {
    register<Copy>("dist") {
        group = "distribution"
        dependsOn(clean, "entry", "bootstrap", "plugin-cs", "plugin-timing")
    }

    register<Copy>("entry") {
        group = "distribution"
        dependsOn(clean)
        dependsOn(":jagent:build")
        val mainProject = project(":jagent")
        // 源路径
        from(mainProject.layout.buildDirectory.file("libs"))
        from(mainProject.layout.projectDirectory.file("README.md"))
        // 目标路径
        into(project.layout.buildDirectory.dir("dist"))
        // 过滤Jar文件
        exclude("*-original.jar")
    }

    register<Copy>("bootstrap") {
        group = "distribution"
        dependsOn(clean)
        dependsOn(":jagent-bootstrap:build")
        val bootstrapProject = project(":jagent-bootstrap")
        // 源路径
        from(bootstrapProject.layout.buildDirectory.file("libs"))
        // 目标路径
        into(project.layout.buildDirectory.dir("dist"))
    }

    register<Copy>("plugin-cs") {
        group = "distribution"
        dependsOn(":plugin-cs:build", "plugin-cs-conf")

        // 插件
        val pluginProject = project(":plugin-cs")
        from(pluginProject.layout.buildDirectory.file("libs"))
        into(project.layout.buildDirectory.dir("dist/plugins"))
    }

    register<Copy>("plugin-cs-conf") {
        group = "distribution"

        // 配置
        val pluginProject = project(":plugin-cs")
        from(pluginProject.layout.files("plugin-cs.toml"))
        into(project.layout.buildDirectory.dir("dist/conf"))
    }

    register<Copy>("plugin-timing") {
        group = "distribution"
        dependsOn(":plugin-timing:build")

        // 插件
        val pluginProject = project(":plugin-timing")
        from(pluginProject.layout.buildDirectory.file("libs"))
        into(project.layout.buildDirectory.dir("dist/plugins"))
    }

}
