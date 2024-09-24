package com.tomtom.sdk.extension.gradle.instrumentation.util

import com.tomtom.sdk.extension.gradle.SdkPlugin
import com.tomtom.sdk.extension.gradle.instrumentation.MethodContext
import com.tomtom.sdk.extension.gradle.util.error
import org.objectweb.asm.MethodVisitor
import org.slf4j.Logger

interface ExceptionHandler {
    fun handle(exception: Throwable)
}

class CatchingMethodVisitor(
    apiVersion: Int,
    prevVisitor: MethodVisitor,
    private val className: String,
    private val methodContext: MethodContext,
    private val exceptionHandler: ExceptionHandler? = null,
    private val logger: Logger = SdkPlugin.logger
) : MethodVisitor(apiVersion, prevVisitor) {

    override fun visitMaxs(maxStack: Int, maxLocals: Int) {
        try {
            super.visitMaxs(maxStack, maxLocals)
        } catch (e: Throwable) {
            exceptionHandler?.handle(e)
            logger.error(e) {
                """
                Error while instrumenting $className.${methodContext.name} ${methodContext.descriptor}.
                Please report this issue to xiaowen.ji@tomtom.com
                """.trimIndent()
            }
            throw e
        }
    }
}
