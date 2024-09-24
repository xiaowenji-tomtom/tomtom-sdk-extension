package com.tomtom.sdk.extension.gradle.util

import org.slf4j.Logger

fun Logger.warn(throwable: Throwable? = null, message: () -> String) {
    warn("[tomtom navsdk extension] ${message()}", throwable)
}

fun Logger.error(throwable: Throwable? = null, message: () -> String) {
    error("[tomtom navsdk extension] ${message()}", throwable)
}

fun Logger.debug(throwable: Throwable? = null, message: () -> String) {
    debug("[tomtom navsdk extension] ${message()}", throwable)
}

fun Logger.info(throwable: Throwable? = null, message: () -> String) {
    info("[tomtom navsdk extension] ${message()}", throwable)
}
