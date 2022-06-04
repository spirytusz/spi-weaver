package com.spirytusz.spi.weaver.transform.scan.directory

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.spirytusz.spi.weaver.config.FileConst
import com.spirytusz.spi.weaver.log.Logger
import com.spirytusz.spi.weaver.transform.data.ServiceImplInfo
import com.spirytusz.spi.weaver.transform.data.ServiceInfo
import com.spirytusz.spi.weaver.transform.extensions.isClassFile
import com.spirytusz.spi.weaver.transform.extensions.relativeFile
import com.spirytusz.spi.weaver.transform.extensions.safelyCopyFile
import com.spirytusz.spi.weaver.transform.scan.TargetClassCollector
import com.spirytusz.spi.weaver.transform.scan.base.AbstractInputScanner
import java.io.File

class FullDirectoryInputScanner(
    private val targetClassCollector: TargetClassCollector
) : AbstractInputScanner() {

    companion object {
        private const val TAG = "FullDirectoryInputScanner"
    }

    override fun onReceiveInput(transformInvocation: TransformInvocation) {
        transformInvocation.inputs.forEach { transformInput ->
            transformInput.directoryInputs.forEach { directoryInput ->
                scanDirectoryInput(transformInvocation.outputProvider, directoryInput)
            }
        }
    }

    private fun scanDirectoryInput(
        outputProvider: TransformOutputProvider,
        directoryInput: DirectoryInput
    ) {
        val directory = directoryInput.file
        val dstDirectory = outputProvider.getContentLocation(
            directoryInput.name.computeDstFileName(),
            directoryInput.contentTypes,
            directoryInput.scopes,
            Format.DIRECTORY
        )
        directoryInput.file.walkTopDown().filter {
            it.isFile && it.isClassFile()
        }.forEach { classFile ->
            val className = classFile.relativeFile(directory).path
            val dstFile = File(
                dstDirectory,
                className.computeDstFileName() + FileConst.CLASS_FILE_SUFFIX
            )
            targetClassCollector.collectForClassFile(className, classFile.inputStream())?.let {
                when (it) {
                    is ServiceInfo -> {
                        Logger.d(TAG) { "scanDirectoryInput() >>> find service ${it.className}" }
                        serviceInfoList.add(it)
                    }
                    is ServiceImplInfo -> {
                        Logger.d(TAG) { "scanDirectoryInput() >>> find service impl ${it.alias} ${it.className}" }
                        serviceImplInfoList.add(it)
                    }
                    else -> {
                        Logger.w(TAG) { "scanDirectoryInput() >>> unknown scan result $it" }
                    }
                }
            }
            classFile.safelyCopyFile(dstFile)
        }
    }
}