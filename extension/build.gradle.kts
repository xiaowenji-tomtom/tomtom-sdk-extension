plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
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
            abiFilters.add("armeabi-v7a")
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
}
