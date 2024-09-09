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
    val artifactoryTomtomgroupComUsername: String by extra
    val artifactoryTomtomgroupComPassword: String by extra
    repositories {
        maven {
            credentials {
                username = artifactoryTomtomgroupComUsername
                password = artifactoryTomtomgroupComPassword
            }
            url = uri("https://artifactory.tomtomgroup.com/artifactory/maven-remotes")
        }
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
