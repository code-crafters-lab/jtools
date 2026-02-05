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


tasks {
    register<Copy>("dist") {
        group = "distribution"
        dependsOn(clean, "entry", "plugin-modulus")
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

    register<Copy>("plugin-modulus") {
        group = "distribution"
        dependsOn(":plugin-modulus:build")

        // 插件
        val pluginProject = project(":plugin-modulus")
        from(pluginProject.layout.buildDirectory.file("libs"))
        into(project.layout.buildDirectory.dir("dist/plugins"))
    }

}
