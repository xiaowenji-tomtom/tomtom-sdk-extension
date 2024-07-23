package com.tomtom.sdk.extension.gradle.instrumentation.tomtom.gesture

import com.android.build.api.instrumentation.ClassContext
import com.tomtom.sdk.extension.gradle.instrumentation.ClassInstrumentable
import com.tomtom.sdk.extension.gradle.instrumentation.CommonClassVisitor
import com.tomtom.sdk.extension.gradle.instrumentation.MethodInstrumentable
import com.tomtom.sdk.extension.gradle.instrumentation.SpanAddingClassVisitorFactory
import org.objectweb.asm.ClassVisitor

class Inertia :
    ClassInstrumentable {

    companion object {
        private const val LOG_CLASSNAME = "ProgressiveGestureDetector"
    }

    override fun getVisitor(
        instrumentableContext: ClassContext,
        apiVersion: Int,
        originalVisitor: ClassVisitor,
        parameters: SpanAddingClassVisitorFactory.SpanAddingParameters
    ): ClassVisitor {
        val methodList: List<MethodInstrumentable> = listOf(
            InertiaMethodInstrumentable()
        )
        return CommonClassVisitor(
            apiVersion,
            originalVisitor,
            LOG_CLASSNAME,
            methodList,
            parameters,
        )
    }

    override fun isInstrumentable(data: ClassContext) =
        data.currentClassData.className == "com.tomtom.sdk.map.gesture.ProgressiveGestureDetector"
}
