package com.spirytusz.spi.weaver.transform.scan.directory

import com.android.build.api.transform.*
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

class IncrementalDirectoryInputScanner(
    private val targetClassCollector: TargetClassCollector
) : AbstractInputScanner() {

    companion object {
        private const val TAG = "IncrementalDirectoryInputScanner"
    }

    override fun onReceiveInput(transformInvocation: TransformInvocation) {
        val outputProvider = transformInvocation.outputProvider
        transformInvocation.inputs.forEach { transformInput ->
            transformInput.directoryInputs.forEach { directoryInput ->
                scanDirectoryInputIncrementally(outputProvider, directoryInput)
            }
        }
    }

    private fun scanDirectoryInputIncrementally(
        outputProvider: TransformOutputProvider,
        directoryInput: DirectoryInput
    ) {
        val srcDirectory = directoryInput.file
        val dstDirectory = outputProvider.getContentLocation(
            directoryInput.name.computeDstFileName(),
            directoryInput.contentTypes,
            directoryInput.scopes,
            Format.DIRECTORY
        )
        val removedFile = directoryInput.changedFiles.filter { (file, _) ->
            file.isFile && file.isClassFile()
        }.filter { (classFile, status) ->
            val className = classFile.relativeFile(srcDirectory).path
            val dstFile = File(
                dstDirectory,
                className.computeDstFileName() + FileConst.CLASS_FILE_SUFFIX
            )
            if (status == Status.REMOVED) {
                dstFile.deleteOnExit()
            }
            status == Status.REMOVED
        }

        directoryInput.file.walkTopDown().filter {
            it.isFile && it.isClassFile()
        }.filter {
            it !in removedFile
        }.forEach { classFile ->
            val className = classFile.relativeFile(srcDirectory).path
            val dstFile = File(
                dstDirectory,
                className.computeDstFileName() + FileConst.CLASS_FILE_SUFFIX
            )
            scanSingleFile(className, classFile)
            classFile.safelyCopyFile(dstFile)
        }
    }

    private fun scanSingleFile(className: String, classFile: File) {
        targetClassCollector.collectForClassFile(className, classFile.inputStream())?.let {
            when (it) {
                is ServiceInfo -> {
                    Logger.d(TAG) { "scanSingleFile() >>> find service ${it.className}" }
                    serviceInfoList.add(it)
                }
                is ServiceImplInfo -> {
                    Logger.d(TAG) { "scanSingleFile() >>> find service impl ${it.alias} ${it.className}" }
                    serviceImplInfoList.add(it)
                }
                else -> {
                    Logger.w(TAG) { "scanSingleJarInput() >>> unknown scan result $it" }
                }
            }
        }
    }
}