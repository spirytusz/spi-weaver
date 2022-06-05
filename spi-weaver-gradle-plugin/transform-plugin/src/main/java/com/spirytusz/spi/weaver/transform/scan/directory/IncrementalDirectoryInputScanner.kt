package com.spirytusz.spi.weaver.transform.scan.directory

import com.android.build.api.transform.*
import com.spirytusz.spi.weaver.config.FileConst
import com.spirytusz.spi.weaver.log.Logger
import com.spirytusz.spi.weaver.transform.cache.CacheManager
import com.spirytusz.spi.weaver.transform.data.ServiceImplInfo
import com.spirytusz.spi.weaver.transform.data.ServiceInfo
import com.spirytusz.spi.weaver.transform.extensions.isClassFile
import com.spirytusz.spi.weaver.transform.extensions.relativeFile
import com.spirytusz.spi.weaver.transform.extensions.safelyCopyFile
import com.spirytusz.spi.weaver.transform.scan.TargetClassCollector
import com.spirytusz.spi.weaver.transform.scan.base.AbstractInputScanner
import java.io.File

class IncrementalDirectoryInputScanner(
    private val targetClassCollector: TargetClassCollector,
    private val cacheManager: CacheManager
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
        val scannedFile = scanChangedFile(directoryInput, dstDirectory)

        directoryInput.file.walkTopDown().filter {
            it.isFile && it.isClassFile()
        }.filter {
            it !in scannedFile
        }.forEach { classFile ->
            val cache = cacheManager.findByPath(classFile.absolutePath)
            if (cache != null) {
                serviceInfoList.addAll(cache.serviceInfoList)
                serviceImplInfoList.addAll(cache.serviceImplInfoList)
            } else {
                // maybe any change here...
                val className = classFile.relativeFile(srcDirectory).path
                val dstFile = File(
                    dstDirectory,
                    className.computeDstFileName() + FileConst.CLASS_FILE_SUFFIX
                )
                scanSingleFile(className, classFile)
                classFile.safelyCopyFile(dstFile)
            }
        }
    }

    private fun scanChangedFile(directoryInput: DirectoryInput, dstDirectory: File): List<File> {
        return directoryInput.changedFiles.filter { (file, _) ->
            file.isFile && file.isClassFile()
        }.map { (srcFile, status) ->
            val path = srcFile.absolutePath
            val className = srcFile.relativeFile(directoryInput.file).path
            val dstFile = File(
                dstDirectory,
                className.computeDstFileName() + FileConst.CLASS_FILE_SUFFIX
            )
            when (status) {
                Status.NOTCHANGED -> {
                    cacheManager.findByPath(path)?.let { cache ->
                        serviceInfoList.addAll(cache.serviceInfoList)
                        serviceImplInfoList.addAll(cache.serviceImplInfoList)
                    }
                }
                Status.ADDED, Status.CHANGED -> {
                    scanSingleFile(className, srcFile)
                    srcFile.safelyCopyFile(dstFile)
                }
                Status.REMOVED -> {
                    cacheManager.invalidateByPath(path)
                    dstFile.deleteOnExit()
                }
                else -> {
                    Logger.w(TAG) { "scanChangedFile() >>> unknown status $status" }
                }
            }
            srcFile
        }
    }

    private fun scanSingleFile(className: String, classFile: File) {
        val path = classFile.absolutePath
        targetClassCollector.collectForClassFile(className, classFile.inputStream())?.let {
            when (it) {
                is ServiceInfo -> {
                    Logger.d(TAG) { "scanSingleFile() >>> find service ${it.className}" }
                    cacheManager.invalidateByPath(path)
                    cacheManager.insertByPath(path, service = it)
                    serviceInfoList.add(it)
                }
                is ServiceImplInfo -> {
                    Logger.d(TAG) { "scanSingleFile() >>> find service impl ${it.alias} ${it.className}" }
                    cacheManager.invalidateByPath(path)
                    cacheManager.insertByPath(path, impl = it)
                    serviceImplInfoList.add(it)
                }
                else -> {
                    Logger.w(TAG) { "scanSingleJarInput() >>> unknown scan result $it" }
                }
            }
        }
    }
}