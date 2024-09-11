plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    kotlin("plugin.serialization") version "2.0.20"
}

publishing {
    repositories {
        mavenLocal()
    }
    publications {
        register<MavenPublication>("release") {
            groupId = "com.tomtom.sdk.extension"
            artifactId = "library"
            version = "0.0.1"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}

android {
    namespace = "com.tomtom.sdk.extension.library"
    compileSdk = 34

    version = "0.0.1"

    buildFeatures {
        prefab = true
    }

    defaultConfig {
        minSdk = 24

        consumerProguardFiles("consumer-rules.pro")
        externalNativeBuild {
            cmake {
                cppFlags("")
            }
        }

        ndk {
//            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packaging {
        jniLibs.excludes.add("**/libshadowhook.so")
    }

    externalNativeBuild {
        cmake {
            path("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("com.bytedance.android:shadowhook:1.0.10")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.tomtom.sdk.http:http-core:1.11.0")
    implementation("com.tomtom.sdk.maps:map-display:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
}

//afterEvaluate {
//    // Define the Conan install task
//    val conanInstall = tasks.register<Exec>("conanInstall") {
//        workingDir = file("${projectDir}/src/main/cpp")
//        commandLine("conan", "install", ".", "--profile", "android_armv8", "--install-folder=${projectDir}/build/conan_build_arm64-v8a", "--build=missing")
//    }
//
//    // Ensure Conan install runs before the externalNativeBuild
//    tasks.named("externalNativeBuildDebug").configure {
//        dependsOn(conanInstall)
//    }
//    tasks.named("externalNativeBuildRelease").configure {
//        dependsOn(conanInstall)
//    }
//}
