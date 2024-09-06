pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.android.application") {
                useModule("com.android.tools.build:gradle:${requested.version}")
            }
            if (requested.id.id == "com.tomtom") {
                useModule("com.tomtom:tomtom-android-gradle-plugin:${requested.version}")
            }
        }
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

rootProject.name = ("tomtom-sdk-extension-composite-build")

includeBuild("plugin-build") {
    dependencySubstitution {
        substitute(module("com.tomtom:tomtom-android-gradle-plugin")).using(project(":"))
    }
}

include(":extension")
