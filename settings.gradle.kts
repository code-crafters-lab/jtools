rootProject.name = "jtools"

pluginManagement {
    includeBuild("/Users/wuyujie/Project/opensource/unity/gradle-plugins")
}

plugins {
    id("ccl.repo")
//    id("ccl.repo") version "0.10.0-beta.3"
//    id("io.github.sgtsilvio.gradle.proguard") version "0.8.0" apply false
}

include("data-guard")
include("demo")

includeBuild("jagent")
//include("other")
include("gcexcel")
