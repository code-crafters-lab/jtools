rootProject.name = "jtools"

pluginManagement {
    repositories {
        mavenLocal()
        maven {
            url = uri("https://packages.aliyun.com/5f6a9b06d24814603933faab/maven/2038604-snapshot-xnrepo")
            credentials {
                val aliyunMavenUsername: String =
                    System.getProperty("dev.opts.aliyun.maven.username", "5f4ba059fa82bfeb805a1e09");
                val aliyunMavenPassword: String = System.getProperty("dev.opts.aliyun.maven.password", "a3XkZLNApybs");

                username = aliyunMavenUsername
                password = aliyunMavenPassword
            }
        }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        gradlePluginPortal()
    }
}

plugins {
    id("ccl.repo") version "0.10.0-beta.3"
//    id("io.github.sgtsilvio.gradle.proguard") version "0.8.0" apply false
}

include("data-guard")
include("crack-agent")
include("demo")
include("JAgent")
//include("other")

include("gcexcel")

include("plugin-modulus")
