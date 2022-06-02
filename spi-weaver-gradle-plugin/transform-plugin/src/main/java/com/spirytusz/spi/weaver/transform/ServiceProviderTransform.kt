package com.spirytusz.spi.weaver.transform

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.gson.GsonBuilder
import com.spirytusz.spi.weaver.config.ConfigProvider
import com.spirytusz.spi.weaver.log.Logger
import com.spirytusz.spi.weaver.transform.scan.InputScanner
import com.spirytusz.spi.weaver.transform.weave.CodeGenerator
import org.gradle.api.Project

class ServiceProviderTransform(
    private val project: Project,
    private val configProvider: ConfigProvider
) : Transform() {

    companion object {
        private const val TAG = "ServiceProviderTransform"
    }

    override fun getName(): String {
        return "ServiceProviderTransform"
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean {
        return true
    }

    override fun transform(transformInvocation: TransformInvocation?) {
        if (transformInvocation == null) {
            Logger.e(TAG) { "transform() >>> transformInvocation is null" }
            throw IllegalArgumentException("transformInvocation is null")
        }

        Logger.isDebuggable = configProvider.debuggable
        Logger.i(TAG) { "transform() >>> config=$configProvider" }

        val inputScanner = InputScanner(project, configProvider)
        inputScanner.onReceiveInput(transformInvocation)
        val serviceMapping = inputScanner.serviceMapping
        Logger.d(TAG) {
            val gson = GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create()
            "serviceMapping: ${gson.toJson(serviceMapping)}"
        }
        val codeGenerator = CodeGenerator(transformInvocation, serviceMapping)
        codeGenerator.generateServiceImplCreators()
        codeGenerator.generateServicePool()
    }

}