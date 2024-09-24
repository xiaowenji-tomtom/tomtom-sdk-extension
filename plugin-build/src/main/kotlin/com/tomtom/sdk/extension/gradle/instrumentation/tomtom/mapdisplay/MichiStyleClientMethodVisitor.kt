package com.tomtom.sdk.extension.gradle.instrumentation.tomtom.mapdisplay

import com.tomtom.sdk.extension.gradle.instrumentation.MethodContext
import com.tomtom.sdk.extension.gradle.instrumentation.MethodInstrumentable
import com.tomtom.sdk.extension.gradle.instrumentation.SpanAddingClassVisitorFactory
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

class MichiStyleClientMethodInstrumentable : MethodInstrumentable {

    override fun getVisitor(
        instrumentableContext: MethodContext,
        apiVersion: Int,
        originalVisitor: MethodVisitor,
        parameters: SpanAddingClassVisitorFactory.SpanAddingParameters
    ): MethodVisitor {
        return MichiStyleClientMethodVisitor(
            apiVersion,
            originalVisitor,
            instrumentableContext,
        )
    }

    override fun isInstrumentable(data: MethodContext): Boolean {
        return data.name == "setLocationMarkerModel" && data.descriptor == "(Landroid/net/Uri;D)V"
    }
}

class MichiStyleClientMethodVisitor(
    apiVersion: Int,
    originalVisitor: MethodVisitor,
    instrumentableContext: MethodContext,
) : AdviceAdapter(
    apiVersion,
    originalVisitor,
    instrumentableContext.access,
    instrumentableContext.name,
    instrumentableContext.descriptor
) {

    private val labelEnd = Label()
    private val labelContinue = Label()

    override fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean
    ) {
        // reloadStyle -> updateMapStyle
        // com.tomtom.sdk.common.functional.Either updateMapStyle(com.tomtom.sdk.map.display.style.infrastructure.StyleHolder):0 -> a

        // Detect the call to a(k2)
        if (opcode == Opcodes.INVOKEVIRTUAL && owner == "com/tomtom/sdk/map/display/internal/T1" && name == "a" && descriptor == "(Lcom/tomtom/sdk/map/display/internal/k2;)Lcom/tomtom/sdk/common/functional/Either;") {
            println("MichiStyleClient visitMethodInsn a(k2)")

            // debug print
            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
            mv.visitLdcInsn("Before calling reloadStyle() -> a(k2)")
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false)

            mv.visitVarInsn(ALOAD, 0) // Load 'this'
            mv.visitFieldInsn(GETFIELD, "com/tomtom/sdk/map/display/internal/T1", "h", "Lcom/tomtom/sdk/map/display/internal/k2;")
            mv.visitJumpInsn(IFNULL, labelEnd)

            // If currentStyle is not null, get isDarkStyle
            mv.visitVarInsn(ALOAD, 0) // Load 'this'
            mv.visitFieldInsn(GETFIELD, "com/tomtom/sdk/map/display/internal/T1", "h", "Lcom/tomtom/sdk/map/display/internal/k2;")
            mv.visitFieldInsn(GETFIELD, "com/tomtom/sdk/map/display/internal/k2", "c", "Z")
            mv.visitJumpInsn(GOTO, labelContinue)

            // If currentStyle is null, use false
            mv.visitLabel(labelEnd)
            mv.visitInsn(ICONST_0)

            // Mark continuation point and call updateMichiStyle with the computed value
            mv.visitLabel(labelContinue)
            mv.visitVarInsn(ALOAD, 0)
            mv.visitInsn(SWAP) // Swap the two top stack values to call the method correctly
            mv.visitMethodInsn(INVOKEVIRTUAL, "com/tomtom/sdk/map/display/internal/T1", "a", "(Z)V", false)
        }
        // Now call the original method
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
    }

    override fun visitMaxs(maxStack: Int, maxLocals: Int) {
        // Calculate maximum stack size and locals
        super.visitMaxs(maxStack + 5, maxLocals + 5)
    }

}
