import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.sentry.android.gradle.internal.ASMifyTask
import io.sentry.android.gradle.internal.BootstrapAndroidSdk
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("dev.gradleplugins.groovy-gradle-plugin") version BuildPluginsVersion.GROOVY_REDISTRIBUTED
    kotlin("jvm") version BuildPluginsVersion.KOTLIN
    id("distribution")
    id("org.jetbrains.dokka") version BuildPluginsVersion.DOKKA
    id("java-gradle-plugin")
    id("maven-publish")
    id("org.jlleitschuh.gradle.ktlint") version BuildPluginsVersion.KTLINT
    // we need this plugin in order to include .aar dependencies into a pure java project, which the gradle plugin is
    id("io.sentry.android.gradle.aar2jar")
    id("com.github.johnrengelman.shadow") version BuildPluginsVersion.SHADOW
    id("com.github.gmazzo.buildconfig") version BuildPluginsVersion.BUILDCONFIG
}

publishing {
    repositories {
        mavenLocal()
    }
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }
}

BootstrapAndroidSdk.locateAndroidSdk(project, extra)

val androidSdkPath: String? by extra

val agp70: SourceSet by sourceSets.creating
val agp74: SourceSet by sourceSets.creating

val shade: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val fixtureClasspath: Configuration by configurations.creating

dependencies {
    agp70.compileOnlyConfigurationName(Libs.GRADLE_API)
    agp70.compileOnlyConfigurationName(Libs.agp("7.0.4"))
    agp70.compileOnlyConfigurationName(project(":common"))

    agp74.compileOnlyConfigurationName(Libs.GRADLE_API)
    agp74.compileOnlyConfigurationName(Libs.agp("7.4.0"))
    agp74.compileOnlyConfigurationName(project(":common"))

    compileOnly(Libs.GRADLE_API)
    compileOnly(Libs.AGP)
    compileOnly(agp70.output)
    compileOnly(agp74.output)
    compileOnly(Libs.PROGUARD)

    implementation(Libs.ASM)
    implementation(Libs.ASM_COMMONS)

    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:${KotlinCompilerVersion.VERSION}")

    // compileOnly since we'll be shading the common dependency into the final jar
    // but we still need to be able to compile it (this also excludes it from .pom)
    compileOnly(project(":common"))
    shade(project(":common"))
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

// We need to compile Groovy first and let Kotlin depend on it.
// See https://docs.gradle.org/6.1-rc-1/release-notes.html#compilation-order
tasks.withType<GroovyCompile>().configureEach {
    sourceCompatibility = JavaVersion.VERSION_11.toString()
    targetCompatibility = JavaVersion.VERSION_11.toString()

    // we don't need the groovy compile task for compatibility source sets
    val ignoreTask = name.contains("agp", ignoreCase = true)
    isEnabled = !ignoreTask
    if (!ignoreTask) {
        classpath = sourceSets["main"].compileClasspath
    }
}

tasks.withType<KotlinCompile>().configureEach {
    if (!name.contains("agp", ignoreCase = true)) {
        libraries.from.addAll(files(sourceSets["main"].groovy.classesDirectory))
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn", "-Xjvm-default=enable")
        languageVersion = "1.8"
        apiVersion = "1.8"
    }
}

// Append any extra dependencies to the test fixtures via a custom configuration classpath. This
// allows us to apply additional plugins in a fixture while still leveraging dependency resolution
// and de-duplication semantics.
tasks.named("pluginUnderTestMetadata").configure {
    (this as PluginUnderTestMetadata).pluginClasspath.from(fixtureClasspath)
}

gradlePlugin {
    plugins {
        register("SdkPlugin") {
            id = "com.tomtom.sdk.extension"
            implementationClass = "com.tomtom.sdk.extension.gradle.SdkPlugin"
        }
    }
}

tasks.withType<Jar> {
    from(agp70.output)
    from(agp74.output)
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
    configurations = listOf(project.configurations.getByName("shade"))

    exclude("/kotlin/**")
    exclude("/groovy**")
    exclude("/org/**")
}

artifacts {
    runtimeOnly(tasks.named("shadowJar"))
    archives(tasks.named("shadowJar"))
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
        // see https://github.com/JLLeitschuh/ktlint-gradle/issues/522#issuecomment-958756817
        exclude { entry ->
            entry.file.toString().contains("generated")
        }
    }
}

val sep = File.separator

distributions {
    main {
        contents {
            from("build${sep}libs")
            from("build${sep}publications${sep}maven")
        }
    }
    create("pluginMarker") {
        contents {
            from("build${sep}publications${sep}pluginMarkerMaven")
        }
    }
}

tasks.named("distZip") {
    dependsOn("publishToMavenLocal")
    onlyIf {
        inputs.sourceFiles.isEmpty.not().also {
            require(it) { "No distribution to zip." }
        }
    }
}

tasks.withType<Test> {
    testLogging {
        events = setOf(
            TestLogEvent.SKIPPED,
            TestLogEvent.PASSED,
            TestLogEvent.FAILED
        )
        showStandardStreams = true
    }
}

tasks.named("processResources")

buildConfig {
    useKotlinOutput()
    packageName("com.tomtom")
    className("BuildConfig")

    buildConfigField("String", "Version", provider { "\"${project.version}\"" })
    buildConfigField("String", "SdkVersion", provider { "\"${project.property("sdk_version")}\"" })
    buildConfigField("String", "AgpVersion", provider { "\"${BuildPluginsVersion.AGP}\"" })
}

tasks.register<ASMifyTask>("asmify")
