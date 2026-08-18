import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.jvm.java

plugins {
    id("ccl.lib")
}

group = "org.codecrafterslab.agent"

subprojects {
    tasks {
        /* 所有子项目均编译为 java 8 */
        withType(JavaCompile::class.java) {
            options.release.set(8)
        }
        withType(KotlinCompile::class.java) {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_1_8)
            }
        }
    }
}

/* distribution 的 clean 需一并清理各插件项目的 build 目录 */
gradle.projectsEvaluated {
    val distribution = project(":distribution")
    val pluginModules = listOf("ep", "cs", "timing")
    pluginModules.forEach { name ->
        findProject(":plugins:plugin-$name")?.let { pluginProject ->
            distribution.tasks.named("clean") {
                dependsOn(pluginProject.tasks.named("clean"))
            }
        }
    }
}
