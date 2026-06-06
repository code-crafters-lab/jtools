plugins {
    id("ccl.lib")
    application
}

group = "org.codecrafterslab"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("ch.qos.logback:logback-classic:1.5.27")
    implementation("com.grapecitysoft.documents:gcexcel:9.0.1")
    implementation("com.google.code.gson:gson:2.12.1")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    compileOnly(project(":jagent-bootstrap"))
//    runtimeOnly(project(":jagent-bootstrap"))

    annotationProcessor("org.projectlombok:lombok:1.18.42")
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    withType<JavaCompile> {
        options.release.set(17)
    }
}

val agent: String =
    project(":distribution").layout.buildDirectory.file("dist/JAgent-1.0.0.jar").get().asFile.absolutePath


/* java agent */
application {
    mainClass.set("GCDemo")
    val args = mutableListOf("-Dfile.encoding=UTF-8")
//    args.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005")
//    args.add("-javaagent:${agent}")
    applicationDefaultJvmArgs = args
}

tasks {
    register("license-raw") {
        description = "准备授权文件"
        group = "demo"
        val f = file("/Users/wuyujie/.local/share/GrapeCity/6bf630ea-22d3-47b5-bb9e-2102f3c52186/.license")
        f.parentFile?.mkdirs()
        f.writeText(
            "NmJmNjMwZWEtMjJkMy00N2I1LWJiOWUtMjEwMmYzYzUyMTg2,NjA2NDExMDdYWFhYWFhYWDA4Mg,bWFjLW1pbmk,RmFsc2U,OTUyNQ,VHJ1ZQ,OTUzMg,OTUzMg,U3RhbmRhcmQ,,;A7eiXxLGFFM7lGGp+ZPmbntKx/ViM6i1JefDezLXqKzYp39Lc8p7GUe8nDSqv3mmq2TedSW5Fxk7WX3sQzfBgVnzt/pMKod1yTZ7StaS6qD7ytS/zpIrMxMjafnrtjVG4M7ZVpIiSzmLUAxOAMrG9R79ZXLi6ZalDK0PQQe9nOc",
            Charsets.UTF_8
        )
        f.delete()
    }

    register("license-fake") {
        description = "准备授权文件"
        group = "demo"
        val f = file("/Users/wuyujie/.local/share/GrapeCity/6bf630ea-22d3-47b5-bb9e-2102f3c52186/.license")
//        f.parentFile?.mkdirs()
//        f.writeText(
//            "NmJmNjMwZWEtMjJkMy00N2I1LWJiOWUtMjEwMmYzYzUyMTg2,MjE0ODY4NzRYWFhYWFhYWDQ3NDI,,dHJ1ZQ,OTUzMg,dHJ1ZQ,MA,MA,U3RhbmRhcmQ,,;ISW6b4W0uGAc0SNqCl3MBCgJWAtESWrNciqtmbWF04ccTF6QkB29WotrlUG62ImHRzqrK5ilTtKjt9k7WIHUy/dcRKx46UKtTPePkGVBHoFPhiRukm9ABSbG5brU8sIIU09RdvDbA4GynZxFFSz+br5+ds38Dn5tYJzXhktOKKPlTNxSa+2fyCM2zLvPVYvzz43+T5Tqi6XIZjGOoZUJ4fPgnwb4j9T6IYo+42wIpBbiZiB+YE8EYYBFqOzZvQULRrDTch92tsw8M8FszueT5OuO1Ra/s5r9Iy5F/sluGLkj4to5F+Rg6v9QqtAxnEPbK4BePbEqaEzqbesS7N3pGQ",
//            Charsets.UTF_8
//        )
    }

    register<JavaExec>("gcexcel") {
        description = "运行 GCExcel 示例"
        dependsOn(":distribution:dist")
        group = "demo"
        mainClass.set("GCDemo")
        classpath = sourceSets.main.get().runtimeClasspath

        val v9File = file(project.layout.projectDirectory.dir("src/test/resources").file("fake.lic"))
        val v9Text = if (v9File.exists()) v9File.readText().trim() else ""
        if (v9Text.isNotEmpty()) {
            environment("GCEXCEL_JAVA_DEPLOY_LICENSE_V9", v9Text)
        }

        jvmArgs = listOf(
            "-Dfile.encoding=UTF-8",
            "-javaagent:${agent}",
            "-Dclass.out.dir=${project.layout.buildDirectory.file("code").get().asFile.absolutePath}",
        )

    }

    register<JavaExec>("gcexcel-debug") {
        description = "调试运行 GCExcel 示例"
        dependsOn(":distribution:dist", "license-fake")
        group = "demo"
        mainClass.set("GCDemo")
        classpath = sourceSets.main.get().runtimeClasspath

        jvmArgs = listOf(
            "-Dfile.encoding=UTF-8",
            "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005",
            "-javaagent:${agent}",
            "-Dclass.out.dir=${project.layout.buildDirectory.file("code").get().asFile.absolutePath}",
        )
    }
}
