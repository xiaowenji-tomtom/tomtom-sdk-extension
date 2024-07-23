package com.tomtom.sdk.extension.gradle.instrumentation.okhttp

import com.android.build.api.instrumentation.ClassContext
import com.tomtom.sdk.extension.gradle.instrumentation.ClassInstrumentable
import com.tomtom.sdk.extension.gradle.instrumentation.CommonClassVisitor
import com.tomtom.sdk.extension.gradle.instrumentation.MethodContext
import com.tomtom.sdk.extension.gradle.instrumentation.MethodInstrumentable
import com.tomtom.sdk.extension.gradle.instrumentation.SpanAddingClassVisitorFactory
import com.tomtom.sdk.extension.gradle.instrumentation.okhttp.visitor.OkHttpEventListenerMethodVisitor
import com.tomtom.sdk.extension.gradle.util.SemVer
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

class OkHttpEventListener(
    private val useSentryAndroidOkHttp: Boolean,
    private val okHttpVersion: SemVer
) : ClassInstrumentable {
    override val fqName: String get() = "okhttp3.OkHttpClient"

    override fun getVisitor(
        instrumentableContext: ClassContext,
        apiVersion: Int,
        originalVisitor: ClassVisitor,
        parameters: SpanAddingClassVisitorFactory.SpanAddingParameters
    ): ClassVisitor = CommonClassVisitor(
        apiVersion = apiVersion,
        classVisitor = originalVisitor,
        className = fqName.substringAfterLast('.'),
        methodInstrumentables = listOf(
            OkHttpEventListenerMethodInstrumentable(
                useSentryAndroidOkHttp,
                okHttpVersion
            )
        ),
        parameters = parameters
    )
}

class OkHttpEventListenerMethodInstrumentable(
    private val useSentryAndroidOkHttp: Boolean,
    private val okHttpVersion: SemVer
) : MethodInstrumentable {
    override val fqName: String get() = "<init>"

    override fun getVisitor(
        instrumentableContext: MethodContext,
        apiVersion: Int,
        originalVisitor: MethodVisitor,
        parameters: SpanAddingClassVisitorFactory.SpanAddingParameters
    ): MethodVisitor = OkHttpEventListenerMethodVisitor(
        apiVersion = apiVersion,
        originalVisitor = originalVisitor,
        instrumentableContext = instrumentableContext,
        okHttpVersion = okHttpVersion,
        useSentryAndroidOkHttp = useSentryAndroidOkHttp
    )

    override fun isInstrumentable(data: MethodContext): Boolean {
        return data.name == fqName && data.descriptor == "(Lokhttp3/OkHttpClient\$Builder;)V"
    }
}
