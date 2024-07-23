package com.tomtom.sdk.extension.gradle

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppExtension
import com.tomtom.sdk.extension.gradle.extensions.SentryPluginExtension
import com.tomtom.sdk.extension.gradle.util.AgpVersions
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.tasks.StopExecutionException
import org.gradle.internal.build.event.BuildEventListenerRegistryInternal
import org.slf4j.LoggerFactory
import java.io.File
import javax.inject.Inject

@Suppress("UnstableApiUsage")
abstract class SdkPlugin @Inject constructor(
    private val buildEvents: BuildEventListenerRegistryInternal
) : Plugin<Project> {

    override fun apply(project: Project) {
        if (AgpVersions.CURRENT < AgpVersions.VERSION_7_0_0) {
            throw StopExecutionException(
                """
                Using com.tomtom.extension.gradle:3+ with Android Gradle Plugin < 7 is not supported.
                Either upgrade the AGP version to 7+, or use an earlier version of the Sentry
                Android Gradle Plugin. For more information check our migration guide
                https://docs.sentry.io/platforms/android/migration/#migrating-from-iosentrytomtom-android-gradle-plugin-2x-to-iosentrytomtom-android-gradle-plugin-300
                """.trimIndent()
            )
        }
        if (!project.plugins.hasPlugin("com.android.application")) {
            project.logger.warn(
                """
                WARNING: Using 'com.tomtom.extension.gradle' is only supported for the app module.
                Please make sure that you apply the Sentry gradle plugin alongside 'com.android.application' on the _module_ level, and not on the root project level.
                https://docs.sentry.io/platforms/android/configuration/gradle/
                """.trimIndent()
            )
        }

        val extension = project.extensions.create(
            "sentry",
            SentryPluginExtension::class.java,
            project
        )

        project.pluginManager.withPlugin("com.android.application") {
            val oldAGPExtension = project.extensions.getByType(AppExtension::class.java)
            val androidComponentsExt =
                project.extensions.getByType(AndroidComponentsExtension::class.java)
            val extraProperties = project.extensions.getByName("ext")
                as ExtraPropertiesExtension

            // new API configuration
            androidComponentsExt.configure(
                project,
                extension,
                buildEvents,
            )

            // old API configuration
            oldAGPExtension.configure(
                project,
                extension,
                buildEvents,
            )
        }
    }

    companion object {
        internal val sep = File.separator

        // a single unified logger used by instrumentation
        internal val logger by lazy {
            LoggerFactory.getLogger(SdkPlugin::class.java)
        }
    }
}
