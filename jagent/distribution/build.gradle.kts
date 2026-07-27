plugins {
    id("ccl.lib")
}

group = "org.codecrafterslab"

tasks {
    register<Copy>("dist") {
        group = "distribution"
        description = "聚合所有子任务，构建完整分发目录"
        dependsOn(clean, "entry", "plugins")
    }

    register<Tar>("archive") {
        group = "distribution"
        description = "打包目录并gzip压缩为tar.gz"
        dependsOn("dist")

        // 开启 gzip
        compression = Compression.GZIP
        archiveFileName.set("jagent-${project.name}-${project.version}.tar.gz")
        destinationDirectory.set(layout.buildDirectory)

        // 要压缩的源文件/目录
        from(layout.buildDirectory.dir("dist"))
    }

    register<Copy>("copy") {
        dependsOn("archive")
        description = "拷贝到 tools 目录"
        group = "distribution"
        from(layout.buildDirectory.file("jagent-${project.name}-${project.version}.tar.gz"))
        into("/Users/wuyujie/Project/jqsoft/teamwork/tools")
    }

    register<Copy>("entry") {
        description = "拷贝核心 jar 及运行时依赖到分发目录"
        group = "distribution"

        val coreProject = project(":core")
        dependsOn(coreProject.tasks.build)

        from(project.layout.projectDirectory.file("README.md"))
        from(coreProject.layout.buildDirectory.file("libs")) {
            exclude("*bootstrap.jar")
            rename { it.replace(Regex("-\\d+\\.\\d+\\.\\d+"), "") }
        }
        from(coreProject.layout.buildDirectory.file("libs")) {
            include("*bootstrap.jar")
            rename { it.replace(Regex("-\\d+\\.\\d+\\.\\d+"), "") }
            into("bootstrap")
        }
        from(coreProject.configurations.runtimeClasspath) {
            into("libs")
        }

        into(project.layout.buildDirectory.dir("dist"))
    }

    register<Copy>("plugins") {
        description = "拷贝所有插件 jar 到分发目录"
        group = "distribution"

        val pluginModules = listOf("ep", "cs", "timing")
        pluginModules.forEach { name ->
            val pluginProject = project.findProject(":plugins:plugin-$name")
            if (pluginProject != null) {
                dependsOn(pluginProject.tasks.named("build"))
                from(pluginProject.layout.buildDirectory.file("libs"))
            }
        }

        into(project.layout.buildDirectory.dir("dist/plugins"))
    }

}
