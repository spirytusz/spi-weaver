package com.spirytusz.spi.weaver.transform.scan.jar

import com.android.build.api.transform.*
import com.spirytusz.spi.weaver.log.Logger
import com.spirytusz.spi.weaver.transform.data.ServiceImplInfo
import com.spirytusz.spi.weaver.transform.data.ServiceInfo
import com.spirytusz.spi.weaver.transform.extensions.safelyCopyFile
import com.spirytusz.spi.weaver.transform.scan.TargetClassCollector
import com.spirytusz.spi.weaver.transform.scan.base.AbstractInputScanner

class IncrementalJarInputScanner(
    private val targetClassCollector: TargetClassCollector
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
        if (jarInput.status == Status.REMOVED) {
            dstFile.deleteOnExit()
            return
        }
        targetClassCollector.collectForJarInput(jarInput).forEach {
            when (it) {
                is ServiceInfo -> {
                    Logger.d(TAG) { "scanSingleJarInputIncrementally() >>> find service ${it.className}" }
                    serviceInfoList.add(it)
                }
                is ServiceImplInfo -> {
                    Logger.d(TAG) { "scanSingleJarInputIncrementally() >>> find service impl ${it.alias} ${it.className}" }
                    serviceImplInfoList.add(it)
                }
                else -> {
                    Logger.w(TAG) { "scanSingleJarInputIncrementally() >>> unknown scan result $it" }
                }
            }
        }
        srcFile.safelyCopyFile(dstFile)
    }
}