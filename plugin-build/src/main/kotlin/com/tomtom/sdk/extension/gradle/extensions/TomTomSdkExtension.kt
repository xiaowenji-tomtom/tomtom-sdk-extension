package com.tomtom.sdk.extension.gradle.extensions

import org.gradle.api.Project
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class TomTomSdkExtension @Inject constructor(project: Project) {
    val shouldDisableInertia: Property<Boolean> = project.objects.property(Boolean::class.java).convention(false)
    val applyLocationMarkerFix_1_11: Property<Boolean> = project.objects.property(Boolean::class.java).convention(false)
}
