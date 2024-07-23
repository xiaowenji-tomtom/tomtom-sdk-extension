plugins {
    kotlin("jvm") version BuildPluginsVersion.KOTLIN apply false
    id("com.android.application") version BuildPluginsVersion.AGP apply false
    id("org.jlleitschuh.gradle.ktlint") version BuildPluginsVersion.KTLINT
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

subprojects {
    apply {
        plugin("org.jlleitschuh.gradle.ktlint")
    }

    ktlint {
        debug.set(false)
        verbose.set(true)
        android.set(true)
        outputToConsole.set(true)
        ignoreFailures.set(false)
        enableExperimentalRules.set(true)
        filter {
            exclude("**/generated/**")
            include("**/kotlin/**")
        }
    }
}

tasks.register("clean", Delete::class.java) {
    delete(rootProject.buildDir)
    dependsOn(gradle.includedBuild("plugin-build").task(":clean"))
}
