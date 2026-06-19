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

tasks {

//    subprojects.map { it.tasks.clean }
    clean {
//        dependsOn(subprojects.map { it.tasks.clean })
    }
}
