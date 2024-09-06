import org.gradle.util.VersionNumber

object BuildPluginsVersion {
    val AGP = System.getenv("VERSION_AGP") ?: "7.4.0"
    const val DOKKA = "1.8.10"
    const val KOTLIN = "1.8.20"
    const val KTLINT = "10.2.1"
    const val SHADOW = "7.1.2"
    // do not upgrade to 0.18.0, it does not generate the pom-default.xml and module.json under
    // build/publications/maven
    const val PROGUARD = "7.1.0"
    const val GROOVY_REDISTRIBUTED = "1.2"
    const val BUILDCONFIG = "3.1.0"

	// proguard does not support AGP 8 yet
    fun isProguardApplicable(): Boolean = VersionNumber.parse(AGP).major < 8
}

object LibsVersion {
    const val ASM = "9.4" // compatibility matrix -> https://developer.android.com/reference/tools/gradle-api/7.1/com/android/build/api/instrumentation/InstrumentationContext#apiversion
}

object Libs {
    fun agp(version: String) = "com.android.tools.build:gradle:$version"
    val AGP = "com.android.tools.build:gradle:${BuildPluginsVersion.AGP}"
    const val PROGUARD = "com.guardsquare:proguard-gradle:${BuildPluginsVersion.PROGUARD}"
    // this allows us to develop against a fixed version of Gradle, as opposed to depending on the
    // locally available version. kotlin-gradle-plugin follows the same approach.
    // More info: https://docs.nokee.dev/manual/gradle-plugin-development-plugin.html
    const val GRADLE_API = "dev.gradleplugins:gradle-api:8.7"

    // bytecode instrumentation
    const val ASM = "org.ow2.asm:asm-util:${LibsVersion.ASM}"
    const val ASM_COMMONS = "org.ow2.asm:asm-commons:${LibsVersion.ASM}"
}
