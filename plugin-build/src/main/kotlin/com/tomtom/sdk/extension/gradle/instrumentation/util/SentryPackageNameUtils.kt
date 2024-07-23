@file:Suppress("UnstableApiUsage")

package com.tomtom.sdk.extension.gradle.instrumentation.util

import com.android.build.api.instrumentation.ClassContext

fun ClassContext.isSentryClass(): Boolean =
    when {
        currentClassData.className.startsWith("io.sentry") &&
            !currentClassData.className.startsWith("io.sentry.samples") &&
            !currentClassData.className.startsWith("io.sentry.mobile") -> true
        else -> false
    }
