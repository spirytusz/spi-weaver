package com.spirytusz.spi.weaver.transform

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.gson.GsonBuilder
import com.spirytusz.spi.weaver.log.Logger
import com.spirytusz.spi.weaver.transform.global.TransformContext
import com.spirytusz.spi.weaver.transform.scan.InputScanner
import com.spirytusz.spi.weaver.transform.scan.ServiceInvalidationAwarer
import com.spirytusz.spi.weaver.transform.validate.ServiceImplConflictDetector
import com.spirytusz.spi.weaver.transform.weave.CodeGenerator

class ServiceProviderTransform(
    private val transformContext: TransformContext
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
        val start = System.currentTimeMillis()
        if (transformInvocation == null) {
            Logger.e(TAG) { "transform() >>> transformInvocation is null" }
            throw IllegalArgumentException("transformInvocation is null")
        }

        val configProvider = transformContext.configProvider
        Logger.isDebuggable = configProvider.debuggable
        Logger.i(TAG) { "transform() >>> config=$configProvider" }

        val invalidateAwarer = ServiceInvalidationAwarer()
        val inputScanner = InputScanner(transformContext, invalidateAwarer)
        inputScanner.onReceiveInput(transformInvocation)
        val serviceMapping = inputScanner.serviceMapping
        val conflictDetector = ServiceImplConflictDetector(serviceMapping)
        conflictDetector.checkOrThrow()
        Logger.d(TAG) {
            val gson = GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create()
            "serviceMapping: ${gson.toJson(serviceMapping)}"
        }
        CodeGenerator(transformInvocation, serviceMapping, invalidateAwarer).generate()
        val end = System.currentTimeMillis()
        Logger.i(TAG) { "transform time cost [${end - start}]ms" }
    }

}