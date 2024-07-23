import com.tomtom.sdk.extension.gradle.instrumentation.MethodContext
import com.tomtom.sdk.extension.gradle.instrumentation.MethodInstrumentable
import com.tomtom.sdk.extension.gradle.instrumentation.SpanAddingClassVisitorFactory
import com.tomtom.sdk.extension.gradle.instrumentation.logcat.LogcatLevel
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter

class LogcatMethodInstrumentable : MethodInstrumentable {

    override fun getVisitor(
        instrumentableContext: MethodContext,
        apiVersion: Int,
        originalVisitor: MethodVisitor,
        parameters: SpanAddingClassVisitorFactory.SpanAddingParameters
    ): MethodVisitor {
        return LogcatMethodVisitor(
            apiVersion,
            originalVisitor,
            instrumentableContext,
            parameters.logcatMinLevel.get()
        )
    }

    override fun isInstrumentable(data: MethodContext): Boolean {
        return true
    }
}

class LogcatMethodVisitor(
    apiVersion: Int,
    originalVisitor: MethodVisitor,
    instrumentableContext: MethodContext,
    private val minLevel: LogcatLevel
) : AdviceAdapter(
    apiVersion,
    originalVisitor,
    instrumentableContext.access,
    instrumentableContext.name,
    instrumentableContext.descriptor
) {

    override fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        desc: String?,
        itf: Boolean
    ) {
        if (shouldReplaceLogCall(owner, name, minLevel)) {
            // Replace call to Log with call to SentryLogcatAdapter
            mv.visitMethodInsn(
                INVOKESTATIC,
                "io/sentry/android/core/SentryLogcatAdapter",
                name,
                desc,
                false
            )
        } else {
            super.visitMethodInsn(opcode, owner, name, desc, itf)
        }
    }

    private fun shouldReplaceLogCall(owner: String, name: String, minLevel: LogcatLevel) =
        LogcatLevel.logFunctionToLevel(name)?.let {
            owner == "android/util/Log" && it.supports(minLevel)
        } ?: false
}
