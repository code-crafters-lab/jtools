rootProject.name = "jagent"

pluginManagement {
    includeBuild("/Users/wuyujie/Project/opensource/unity/gradle-plugins")
}

plugins {
    id("ccl.repo")
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

include("core")
include("distribution")
include("plugins:plugin-ep")
