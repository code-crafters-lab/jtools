plugins {
    id("ccl.lib")
}

group = "org.codecrafterslab"

tasks {
    register<Copy>("libs") {
        group = "distribution"
        description = "拷贝核心依赖"

        val coreProject = project(":core")
        from(coreProject.configurations.runtimeClasspath)
        into(project.layout.buildDirectory.dir("dist/libs"))
    }

    register<Copy>("dist") {
        group = "distribution"
        description = ""
        dependsOn(clean, "entry", "libs", "plugin-ep")
    }

    register<Tar>("archive") {
        group = "distribution"
        description = "打包目录并gzip压缩为tar.gz"
        dependsOn("dist")

        // 开启gzip
        compression = Compression.GZIP
        archiveFileName.set("jagent-${project.name}-${project.version}.tar.gz")
        destinationDirectory.set(layout.buildDirectory)

        // 要压缩的源文件/目录
        from(layout.buildDirectory.dir("dist"))
    }

    register<Copy>("entry") {
        description = "代理入库"
        group = "distribution"
        val coreProject = project(":core")
        dependsOn(coreProject.tasks.build)
        from(coreProject.layout.buildDirectory.file("libs"))

        val jagentProject = project(":")
        from(jagentProject.layout.projectDirectory.file("README.md"))
        into(project.layout.buildDirectory.dir("dist"))
    }

    register<Copy>("plugin-ep") {
        description = "环境公钥适配插件"
        group = "distribution"
        val pluginProject = project(":plugins:plugin-ep")
        dependsOn(pluginProject.tasks.build)

        from(pluginProject.layout.buildDirectory.file("libs"))
        into(project.layout.buildDirectory.dir("dist/plugins"))
    }

//    register<Copy>("plugin-cs") {
//        group = "distribution"
//        dependsOn(":plugins:plugin-cs:build", "plugin-cs-conf")
//
//        val pluginProject = project(":plugins:plugin-cs")
//        from(pluginProject.layout.buildDirectory.file("libs"))
//        into(project.layout.buildDirectory.dir("dist/plugins"))
//    }
//
//    register<Copy>("plugin-cs-conf") {
//        group = "distribution"
//
//        val pluginProject = project(":plugins:plugin-cs")
//        from(pluginProject.layout.files("plugin-cs.toml"))
//        into(project.layout.buildDirectory.dir("dist/conf"))
//    }
//
//    register<Copy>("plugin-timing") {
//        group = "distribution"
//        dependsOn(":plugins:plugin-timing:build")
//
//        val pluginProject = project(":plugins:plugin-timing")
//        from(pluginProject.layout.buildDirectory.file("libs"))
//        into(project.layout.buildDirectory.dir("dist/plugins"))
//    }

}
