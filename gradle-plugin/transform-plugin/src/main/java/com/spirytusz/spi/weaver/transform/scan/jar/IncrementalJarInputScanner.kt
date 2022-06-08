package com.spirytusz.spi.weaver.transform.scan.jar

import com.android.build.api.transform.*
import com.spirytusz.spi.weaver.log.Logger
import com.spirytusz.spi.weaver.transform.cache.CacheManager
import com.spirytusz.spi.weaver.transform.data.ServiceImplInfo
import com.spirytusz.spi.weaver.transform.data.ServiceInfo
import com.spirytusz.spi.weaver.transform.extensions.safelyCopyFile
import com.spirytusz.spi.weaver.transform.scan.TargetClassCollector
import com.spirytusz.spi.weaver.transform.scan.base.AbstractInputScanner

class IncrementalJarInputScanner(
    private val targetClassCollector: TargetClassCollector,
    private val cacheManager: CacheManager
) : AbstractInputScanner() {

    companion object {
        private const val TAG = "IncrementalJarInputScanner"
    }

    override fun onReceiveInput(transformInvocation: TransformInvocation) {
        val outputProvider = transformInvocation.outputProvider
        transformInvocation.inputs.forEach { transformInput ->
            transformInput.jarInputs.forEach { jarInput ->
                scanSingleJarInputIncrementally(outputProvider, jarInput)
            }
        }
    }

    private fun scanSingleJarInputIncrementally(
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
        val path = srcFile.absolutePath
        when (val status = jarInput.status) {
            Status.NOTCHANGED -> {
                cacheManager.findByPath(path)?.let { cache ->
                    serviceInfoList.addAll(cache.serviceInfoList)
                    serviceImplInfoList.addAll(cache.serviceImplInfoList)
                }
            }
            Status.ADDED, Status.CHANGED -> {
                scanSingleJarInput(jarInput)
            }
            Status.REMOVED -> {
                dstFile.deleteOnExit()
                cacheManager.invalidateByPath(path)
            }
            else -> {
                Logger.w(TAG) { "scanSingleJarInputIncrementally() >>> unknown status $status" }
            }
        }

        srcFile.safelyCopyFile(dstFile)
    }

    private fun scanSingleJarInput(jarInput: JarInput) {
        val path = jarInput.file.absolutePath
        targetClassCollector.collectForJarInput(jarInput).forEach {
            when (it) {
                is ServiceInfo -> {
                    Logger.d(TAG) { "scanSingleJarInputIncrementally() >>> find service ${it.className}" }
                    cacheManager.insertByPath(path, service = it)
                    serviceInfoList.add(it)
                }
                is ServiceImplInfo -> {
                    Logger.d(TAG) { "scanSingleJarInputIncrementally() >>> find service impl ${it.alias} ${it.className}" }
                    cacheManager.insertByPath(path, impl = it)
                    serviceImplInfoList.add(it)
                }
                else -> {
                    Logger.w(TAG) { "scanSingleJarInputIncrementally() >>> unknown scan result $it" }
                }
            }
        }
    }
}