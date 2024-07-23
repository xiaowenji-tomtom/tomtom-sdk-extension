package com.tomtom.sdk.extension.gradle.instrumentation.tomtom.gesture

import com.tomtom.sdk.extension.gradle.instrumentation.MethodContext
import com.tomtom.sdk.extension.gradle.instrumentation.MethodInstrumentable
import com.tomtom.sdk.extension.gradle.instrumentation.SpanAddingClassVisitorFactory
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

class InertiaMethodInstrumentable : MethodInstrumentable {

    override fun getVisitor(
        instrumentableContext: MethodContext,
        apiVersion: Int,
        originalVisitor: MethodVisitor,
        parameters: SpanAddingClassVisitorFactory.SpanAddingParameters
    ): MethodVisitor {
        return InertiaMethodVisitor(
            apiVersion,
            originalVisitor,
            instrumentableContext,
        )
    }

    override fun isInstrumentable(data: MethodContext) =
        data.name == "<init>"
}

class InertiaMethodVisitor(
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

    override fun visitInsn(opcode: Int) {
        if (opcode == Opcodes.RETURN) {
            mv.visitVarInsn(Opcodes.ALOAD, 0) // Load 'this' onto the stack
            mv.visitLdcInsn(1.0f) // Load the new float value onto the stack
            mv.visitFieldInsn(Opcodes.PUTFIELD, "com/tomtom/sdk/map/gesture/ProgressiveGestureDetector", "maxFlingVelocity", "F") // Set the field

            mv.visitVarInsn(Opcodes.ALOAD, 0) // Load 'this' onto the stack
            mv.visitLdcInsn(1) // Load the new int value onto the stack
            mv.visitFieldInsn(Opcodes.PUTFIELD, "com/tomtom/sdk/map/gesture/ProgressiveGestureDetector", "minFlingVelocity", "I") // Set the field
        }
        super.visitInsn(opcode)
    }
}
