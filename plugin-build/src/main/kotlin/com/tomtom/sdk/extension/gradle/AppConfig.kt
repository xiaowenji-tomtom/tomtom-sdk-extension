package com.tomtom.sdk.extension.gradle

import com.android.build.gradle.AppExtension
import com.tomtom.sdk.extension.gradle.SentryTasksProvider.getLintVitalAnalyzeProvider
import com.tomtom.sdk.extension.gradle.SentryTasksProvider.getLintVitalReportProvider
import com.tomtom.sdk.extension.gradle.SentryTasksProvider.getMergeAssetsProvider
import com.tomtom.sdk.extension.gradle.extensions.SentryPluginExtension
import com.tomtom.sdk.extension.gradle.util.SentryPluginUtils.isVariantAllowed
import com.tomtom.sdk.extension.gradle.util.SentryPluginUtils.withLogging
import org.gradle.api.Project
import org.gradle.internal.build.event.BuildEventListenerRegistryInternal

fun AppExtension.configure(
    project: Project,
    extension: SentryPluginExtension,
    buildEvents: BuildEventListenerRegistryInternal
) {
    applicationVariants.matching {
        isVariantAllowed(extension, it.name, it.flavorName, it.buildType.name)
    }.configureEach { variant ->
        val mergeAssetsDependants = setOf(
            getMergeAssetsProvider(variant),
            // lint vital tasks scan the entire "build" folder; since we're writing our
            // generated stuff in there, we put explicit dependency on them to avoid
            // warnings about implicit dependency
            withLogging(project.logger, "lintVitalAnalyzeTask") {
                getLintVitalAnalyzeProvider(project, variant.name)
            },
            withLogging(project.logger, "lintVitalReportTask") {
                getLintVitalReportProvider(project, variant.name)
            }
        )
    }
}
