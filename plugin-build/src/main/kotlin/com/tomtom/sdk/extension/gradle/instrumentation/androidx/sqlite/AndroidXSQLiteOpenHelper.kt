package com.tomtom.sdk.extension.gradle.instrumentation.androidx.sqlite

import com.android.build.api.instrumentation.ClassContext
import com.tomtom.sdk.extension.gradle.instrumentation.ClassInstrumentable
import com.tomtom.sdk.extension.gradle.instrumentation.CommonClassVisitor
import com.tomtom.sdk.extension.gradle.instrumentation.MethodContext
import com.tomtom.sdk.extension.gradle.instrumentation.MethodInstrumentable
import com.tomtom.sdk.extension.gradle.instrumentation.SpanAddingClassVisitorFactory
import com.tomtom.sdk.extension.gradle.instrumentation.androidx.sqlite.visitor.SQLiteOpenHelperMethodVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

class AndroidXSQLiteOpenHelper : ClassInstrumentable {

    override fun getVisitor(
        instrumentableContext: ClassContext,
        apiVersion: Int,
        originalVisitor: ClassVisitor,
        parameters: SpanAddingClassVisitorFactory.SpanAddingParameters
    ): ClassVisitor {
        val currentClassName = instrumentableContext.currentClassData.className
        val sqLiteMethodList: List<MethodInstrumentable> = listOf(
            SQLiteOpenHelperMethodInstrumentable()
        )
        return CommonClassVisitor(
            apiVersion,
            originalVisitor,
            currentClassName.substringAfterLast('.'),
            sqLiteMethodList,
            parameters
        )
    }

    // Instrument any class implementing androidx.sqlite.db.SupportSQLiteOpenHelper$Factory
    override fun isInstrumentable(data: ClassContext) =
        data.currentClassData.interfaces.contains(
            "androidx.sqlite.db.SupportSQLiteOpenHelper\$Factory"
        )
}

class SQLiteOpenHelperMethodInstrumentable : MethodInstrumentable {

    override fun getVisitor(
        instrumentableContext: MethodContext,
        apiVersion: Int,
        originalVisitor: MethodVisitor,
        parameters: SpanAddingClassVisitorFactory.SpanAddingParameters
    ): MethodVisitor {
        return SQLiteOpenHelperMethodVisitor(
            apiVersion,
            originalVisitor,
            instrumentableContext
        )
    }

    // We want to instrument only the SupportSQLiteOpenHelper.Factory method
    //  fun create(config: SupportSQLiteOpenHelper.Configuration): SupportSQLiteOpenHelper {...}
    override fun isInstrumentable(data: MethodContext) =
        data.name == "create" &&
            data.descriptor == "(Landroidx/sqlite/db/SupportSQLiteOpenHelper\$Configuration;)" +
            "Landroidx/sqlite/db/SupportSQLiteOpenHelper;"
}
