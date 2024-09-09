package com.tomtom.sdk.extension.gradle.instrumentation

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import com.tomtom.sdk.extension.gradle.SdkPlugin
import com.tomtom.sdk.extension.gradle.instrumentation.logcat.LogcatLevel
import com.tomtom.sdk.extension.gradle.instrumentation.tomtom.gesture.Inertia
import com.tomtom.sdk.extension.gradle.instrumentation.util.findClassReader
import com.tomtom.sdk.extension.gradle.instrumentation.util.findClassWriter
import com.tomtom.sdk.extension.gradle.instrumentation.util.isMinifiedClass
import com.tomtom.sdk.extension.gradle.services.SentryModulesService
import com.tomtom.sdk.extension.gradle.util.SemVer
import com.tomtom.sdk.extension.gradle.util.SentryModules
import com.tomtom.sdk.extension.gradle.util.SentryVersions
import com.tomtom.sdk.extension.gradle.util.info
import java.io.File
import org.gradle.api.internal.artifacts.DefaultModuleIdentifier
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.objectweb.asm.ClassVisitor

@Suppress("UnstableApiUsage")
abstract class SpanAddingClassVisitorFactory :
    AsmClassVisitorFactory<SpanAddingClassVisitorFactory.SpanAddingParameters> {

    interface SpanAddingParameters : InstrumentationParameters {

        /**
         * AGP will re-instrument dependencies, when the [InstrumentationParameters] changed
         * https://issuetracker.google.com/issues/190082518#comment4. This is just a dummy parameter
         * that is used solely for that purpose.
         */
        @get:Input
        @get:Optional
        val invalidate: Property<Long>

        @get:Input
        val debug: Property<Boolean>

        @get:Input
        val logcatMinLevel: Property<LogcatLevel>

        @get:Internal
        val sentryModulesService: Property<SentryModulesService>

        @get:Internal
        val tmpDir: Property<File>

        @get:Internal
        var _instrumentable: ClassInstrumentable?
    }

    private val instrumentable: ClassInstrumentable
        get() {
            val memoized = parameters.get()._instrumentable
            if (memoized != null) {
                return memoized
            }
            val instrumentableList = mutableListOf<ClassInstrumentable>()
            if (SdkPlugin.sdkExtensionConfigs["shouldDisableInertia"] == true) {
                instrumentableList.add(Inertia())
            }
            val instrumentable = ChainedInstrumentable(instrumentableList)
            SdkPlugin.logger.info {
                "Instrumentable: $instrumentable"
            }
            parameters.get()._instrumentable = instrumentable
            return instrumentable
        }

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        val className = classContext.currentClassData.className
        val classReader = nextClassVisitor.findClassWriter()?.findClassReader()
        val isMinifiedClass = classReader?.isMinifiedClass() ?: false

        println("visit class: $className, minified=$isMinifiedClass")

        return instrumentable.getVisitor(
            classContext,
            instrumentationContext.apiVersion.get(),
            nextClassVisitor,
            parameters = parameters.get()
        )
    }

    override fun isInstrumentable(classData: ClassData): Boolean =
        instrumentable.isInstrumentable(classData.toClassContext())
}
