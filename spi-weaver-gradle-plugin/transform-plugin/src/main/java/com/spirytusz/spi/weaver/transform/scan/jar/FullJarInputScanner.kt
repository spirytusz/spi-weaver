package com.spirytusz.spi.weaver.transform.scan.jar

import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.spirytusz.spi.weaver.log.Logger
import com.spirytusz.spi.weaver.transform.cache.CacheManager
import com.spirytusz.spi.weaver.transform.data.ServiceImplInfo
import com.spirytusz.spi.weaver.transform.data.ServiceInfo
import com.spirytusz.spi.weaver.transform.extensions.safelyCopyFile
import com.spirytusz.spi.weaver.transform.scan.TargetClassCollector
import com.spirytusz.spi.weaver.transform.scan.base.AbstractInputScanner

class FullJarInputScanner(
    private val targetClassCollector: TargetClassCollector,
    private val cacheManager: CacheManager
) : AbstractInputScanner() {

    companion object {
        private const val TAG = "FullJarInputScanner"
    }

    override fun onReceiveInput(transformInvocation: TransformInvocation) {
        transformInvocation.inputs.forEach { transformInput ->
            transformInput.jarInputs.forEach { jarInput ->
                scanSingleJarInput(transformInvocation.outputProvider, jarInput)
            }
        }
    }

    private fun scanSingleJarInput(
        outputProvider: TransformOutputProvider,
        jarInput: JarInput
    ) {
        val srcFile = jarInput.file
        val dstFile = outputProvider.getContentLocation(
            jarInput.name.computeDstFileName(),
            jarInput.contentTypes,
            jarInput.scopes,
            Format.JAR
        )
        targetClassCollector.collectForJarInput(jarInput).forEach {
            when (it) {
                is ServiceInfo -> {
                    Logger.d(TAG) { "scanSingleJarInput() >>> find service ${it.className}" }
                    cacheManager.insertByPath(srcFile.absolutePath, service = it)
                    serviceInfoList.add(it)
                }
                is ServiceImplInfo -> {
                    Logger.d(TAG) { "scanSingleJarInput() >>> find service impl ${it.alias} ${it.className}" }
                    cacheManager.insertByPath(srcFile.absolutePath, impl = it)
                    serviceImplInfoList.add(it)
                }
                else -> {
                    Logger.w(TAG) { "scanSingleJarInput() >>> unknown scan result $it" }
                }
            }
        }
        srcFile.safelyCopyFile(dstFile)
    }
}