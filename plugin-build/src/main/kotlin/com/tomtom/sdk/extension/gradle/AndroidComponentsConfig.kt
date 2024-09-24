@file:Suppress("UnstableApiUsage")

package com.tomtom.sdk.extension.gradle

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationParameters
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.android.build.gradle.internal.utils.setDisallowChanges
import com.tomtom.sdk.extension.gradle.SdkPlugin.Companion.sep
import com.tomtom.sdk.extension.gradle.extensions.SentryPluginExtension
import com.tomtom.sdk.extension.gradle.instrumentation.SpanAddingClassVisitorFactory
import com.tomtom.sdk.extension.gradle.services.SentryModulesService
import com.tomtom.sdk.extension.gradle.transforms.MetaInfStripTransform
import com.tomtom.sdk.extension.gradle.util.AgpVersions
import com.tomtom.sdk.extension.gradle.util.AgpVersions.isAGP74
import com.tomtom.sdk.extension.gradle.util.SentryPluginUtils.isVariantAllowed
import com.tomtom.sdk.extension.gradle.util.collectModules
import org.gradle.api.Project
import org.gradle.api.provider.SetProperty
import org.gradle.internal.build.event.BuildEventListenerRegistryInternal
import java.io.File

fun AndroidComponentsExtension<*, *, *>.configure(
    project: Project,
    extension: SentryPluginExtension,
    buildEvents: BuildEventListenerRegistryInternal,
) {
    println("project: ${project.displayName} aka ${project.name} at ${project.path}")
    // temp folder for sentry-related stuff
    val tmpDir = File("${project.buildDir}${sep}tmp${sep}sentry")
    tmpDir.mkdirs()

    configureVariants { variant ->
        println("variant: ${variant.name}")
        if (isVariantAllowed(extension, variant.name, variant.flavorName, variant.buildType)) {
            if (extension.tracingInstrumentation.enabled.get()) {
                /**
                 * We detect sentry-android SDK version using configurations.incoming.afterResolve.
                 * This is guaranteed to be executed BEFORE any of the build tasks/transforms are started.
                 *
                 * After detecting the sdk state, we use Gradle's shared build service to persist
                 * the state between builds and also during a single build, because transforms
                 * are run in parallel.
                 */
                val sentryModulesService = SentryModulesService.register(
                    project,
                    extension.tracingInstrumentation.features,
                    extension.includeSourceContext,
                    extension.dexguardEnabled,
                    extension.tracingInstrumentation.appStart.enabled
                )
                /**
                 * We have to register SentryModulesService as a build event listener, so it will
                 * not be discarded after the configuration phase (where we store the collected
                 * dependencies), and will be passed down to the InstrumentationFactory
                 */
                buildEvents.onTaskCompletion(sentryModulesService)

                project.collectModules(
                    "${variant.name}RuntimeClasspath",
                    variant.name,
                    sentryModulesService
                )

                variant.configureInstrumentation(
                    SpanAddingClassVisitorFactory::class.java,
                    InstrumentationScope.ALL,
                    FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS,
                    extension.tracingInstrumentation.excludes
                ) { params ->
                    if (extension.tracingInstrumentation.forceInstrumentDependencies.get()) {
                        params.invalidate.setDisallowChanges(System.currentTimeMillis())
                    }
                    params.debug.setDisallowChanges(
                        extension.tracingInstrumentation.debug.get()
                    )
                    params.sentryModulesService.setDisallowChanges(sentryModulesService)
                    params.tmpDir.set(tmpDir)
                }

                /**
                 * This necessary to address the issue when target app uses a multi-release jar
                 * (MR-JAR) as a dependency. https://github.com/getsentry/tomtom-android-gradle-plugin/issues/256
                 *
                 * We register a transform (https://docs.gradle.org/current/userguide/artifact_transforms.html)
                 * that will strip-out unnecessary files from the MR-JAR, so the AGP transforms
                 * will consume corrected artifacts. We only do this when auto-instrumentation is
                 * enabled (otherwise there's no need in this fix) AND when AGP version
                 * is below 7.1.2, where this issue has been fixed.
                 * (https://androidstudio.googleblog.com/2022/02/android-studio-bumblebee-202111-patch-2.html)
                 */
                if (AgpVersions.CURRENT < AgpVersions.VERSION_7_1_2) {
                    // we are only interested in runtime configuration (as ASM transform is
                    // also run just for the runtime configuration)
                    project.configurations.named("${variant.name}RuntimeClasspath")
                        .configure {
                            it.attributes.attribute(MetaInfStripTransform.metaInfStripped, true)
                        }
                    MetaInfStripTransform.register(
                        project.dependencies,
                        extension.tracingInstrumentation.forceInstrumentDependencies.get()
                    )
                }
            }
        }
    }
}

private fun <T : InstrumentationParameters> Variant.configureInstrumentation(
    classVisitorFactoryImplClass: Class<out AsmClassVisitorFactory<T>>,
    scope: InstrumentationScope,
    mode: FramesComputationMode,
    excludes: SetProperty<String>,
    instrumentationParamsConfig: (T) -> Unit,
) {
    if (isAGP74) {
        configureInstrumentationFor74(
            variant = this,
            classVisitorFactoryImplClass,
            scope,
            mode,
            excludes,
            instrumentationParamsConfig
        )
    } else {
        configureInstrumentationFor70(
            variant = this,
            classVisitorFactoryImplClass,
            scope,
            mode,
            instrumentationParamsConfig
        )
    }
}

/**
 * onVariants method in AGP 7.4.0 has a binary incompatibility with the prior versions, hence we
 * have to distinguish here, although the compatibility sources would look exactly the same.
 */
private fun AndroidComponentsExtension<*, *, *>.configureVariants(callback: (Variant) -> Unit) {
    if (isAGP74) {
        onVariants74(this, callback)
    } else {
        onVariants70(this, callback)
    }
}
