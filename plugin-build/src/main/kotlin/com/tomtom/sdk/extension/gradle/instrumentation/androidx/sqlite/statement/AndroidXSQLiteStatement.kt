@file:Suppress("UnstableApiUsage")

package com.tomtom.sdk.extension.gradle.instrumentation.androidx.sqlite.statement

import com.android.build.api.instrumentation.ClassContext
import com.tomtom.sdk.extension.gradle.instrumentation.ClassInstrumentable
import com.tomtom.sdk.extension.gradle.instrumentation.CommonClassVisitor
import com.tomtom.sdk.extension.gradle.instrumentation.MethodContext
import com.tomtom.sdk.extension.gradle.instrumentation.MethodInstrumentable
import com.tomtom.sdk.extension.gradle.instrumentation.ReturnType
import com.tomtom.sdk.extension.gradle.instrumentation.SpanAddingClassVisitorFactory
import com.tomtom.sdk.extension.gradle.instrumentation.androidx.sqlite.statement.visitor.ExecuteStatementMethodVisitor
import com.tomtom.sdk.extension.gradle.util.SemVer
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

class AndroidXSQLiteStatement(private val androidXSqliteVersion: SemVer) : ClassInstrumentable {

    override val fqName: String get() = "androidx.sqlite.db.framework.FrameworkSQLiteStatement"

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
            ExecuteInsert(androidXSqliteVersion),
            ExecuteUpdateDelete(androidXSqliteVersion)
        ),
        parameters = parameters
    )
}

class ExecuteInsert(private val androidXSqliteVersion: SemVer) : MethodInstrumentable {
    override val fqName: String get() = "executeInsert"

    override fun getVisitor(
        instrumentableContext: MethodContext,
        apiVersion: Int,
        originalVisitor: MethodVisitor,
        parameters: SpanAddingClassVisitorFactory.SpanAddingParameters
    ): MethodVisitor = ExecuteStatementMethodVisitor(
        ReturnType.LONG,
        apiVersion,
        originalVisitor,
        instrumentableContext.access,
        instrumentableContext.descriptor,
        androidXSqliteVersion
    )
}

class ExecuteUpdateDelete(private val androidXSqliteVersion: SemVer) : MethodInstrumentable {
    override val fqName: String get() = "executeUpdateDelete"

    override fun getVisitor(
        instrumentableContext: MethodContext,
        apiVersion: Int,
        originalVisitor: MethodVisitor,
        parameters: SpanAddingClassVisitorFactory.SpanAddingParameters
    ): MethodVisitor = ExecuteStatementMethodVisitor(
        ReturnType.INTEGER,
        apiVersion,
        originalVisitor,
        instrumentableContext.access,
        instrumentableContext.descriptor,
        androidXSqliteVersion
    )
}
