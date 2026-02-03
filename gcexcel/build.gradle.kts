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
    implementation("ch.qos.logback:logback-classic:1.5.18")
    implementation("com.grapecitysoft.documents:gcexcel:9.0.1")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

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

/* java agent */
application {
    mainClass.set("GCDemo")
    val args = mutableListOf("-Dfile.encoding=UTF-8")
    args.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005")
    args.add("-javaagent:/Users/wuyujie/Project/opensource/jvmti-tools/ja-netfilter/build/libs/JAgent-1.0.0.jar")
    applicationDefaultJvmArgs = args
}
