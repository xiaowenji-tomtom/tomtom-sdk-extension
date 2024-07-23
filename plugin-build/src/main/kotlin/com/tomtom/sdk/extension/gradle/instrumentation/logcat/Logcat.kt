package com.tomtom.sdk.extension.gradle.instrumentation.logcat

import LogcatMethodInstrumentable
import com.android.build.api.instrumentation.ClassContext
import com.tomtom.sdk.extension.gradle.instrumentation.ClassInstrumentable
import com.tomtom.sdk.extension.gradle.instrumentation.CommonClassVisitor
import com.tomtom.sdk.extension.gradle.instrumentation.MethodInstrumentable
import com.tomtom.sdk.extension.gradle.instrumentation.SpanAddingClassVisitorFactory
import com.tomtom.sdk.extension.gradle.instrumentation.util.isSentryClass
import org.objectweb.asm.ClassVisitor

class Logcat :
    ClassInstrumentable {

    companion object {
        private const val LOG_CLASSNAME = "Log"
    }

    override fun getVisitor(
        instrumentableContext: ClassContext,
        apiVersion: Int,
        originalVisitor: ClassVisitor,
        parameters: SpanAddingClassVisitorFactory.SpanAddingParameters
    ): ClassVisitor {
        val logcatMethodList: List<MethodInstrumentable> = listOf(
            LogcatMethodInstrumentable()
        )
        return CommonClassVisitor(
            apiVersion,
            originalVisitor,
            LOG_CLASSNAME,
            logcatMethodList,
            parameters
        )
    }

    override fun isInstrumentable(data: ClassContext) =
        !data.isSentryClass()
}
