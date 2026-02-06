plugins {
    id("ccl.lib")
    application
}

group = "org.codecrafterslab"

repositories {
    mavenCentral()
}
val obfuscated = true

dependencies {
    if (obfuscated) {
        val project1 = findProject(":data-guard")
        val file = project1?.layout?.buildDirectory?.file("libs/${project1.name}-obfuscated.jar")
        implementation(files(file!!))
    } else {
        implementation(project(":data-guard"))
    }
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("ch.qos.logback:logback-classic:1.5.18")
    implementation("com.beust:jcommander:1.82")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    annotationProcessor("org.projectlombok:lombok:1.18.38")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

var javaLibraryPath = ""

enum class AgentType { JAVA, NATIVE }

var agentType = AgentType.JAVA
/* java agent */
//val agentJar = project(":crack-agent").tasks.jar.get().outputs.files.singleFile
/* native agent */
val agentPath = "${javaLibraryPath}/agent.dll"

application {
    mainClass.set("org.codecrafterslab.App")
    val args = mutableListOf("-Dfile.encoding=UTF-8")

//    if (agentJar.exists() && agentType == AgentType.JAVA) {
//        args.add("-javaagent:${agentJar.absolutePath}")
//    }

    if (agentPath.isNotEmpty() && agentType == AgentType.NATIVE) {
        args.add("-agentpath:${agentPath}")
    }

    if (javaLibraryPath.isNotEmpty()) {
        args.add("-Djava.library.path=${javaLibraryPath}")
    }

    args.add("-Dtransformer.class.name=org.codecrafterslab.data.a.a")

    applicationDefaultJvmArgs = args
}

