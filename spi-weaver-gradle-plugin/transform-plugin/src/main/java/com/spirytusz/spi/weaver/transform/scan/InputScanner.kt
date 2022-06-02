package com.spirytusz.spi.weaver.transform.scan

import com.android.build.api.transform.Format
import com.android.build.api.transform.Status
import com.android.build.api.transform.TransformInvocation
import com.spirytusz.spi.weaver.config.ConfigProvider
import com.spirytusz.spi.weaver.config.FileConst.CLASS_FILE_SUFFIX
import com.spirytusz.spi.weaver.log.Logger
import com.spirytusz.spi.weaver.transform.data.ServiceImplInfo
import com.spirytusz.spi.weaver.transform.data.ServiceInfo
import com.spirytusz.spi.weaver.transform.extensions.isClassFile
import com.spirytusz.spi.weaver.transform.extensions.relativeFile
import com.spirytusz.spi.weaver.transform.extensions.safelyCopyFile
import com.spirytusz.spi.weaver.transform.extensions.safelyDelete
import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Project
import java.io.File

class InputScanner(private val project: Project, private val configProvider: ConfigProvider) {

    companion object {
        private const val TAG = "InputScanner"
    }

    private val targetClassCollector = TargetClassCollector(ClassFilter(configProvider))

    val serviceMapping: Map<ServiceInfo, List<ServiceImplInfo>>
        get() = computeServiceImplRelationship()

    fun onReceiveInput(transformInvocation: TransformInvocation) {
        Logger.i(TAG) { "onReceiveInput() >>> scan start, isIncremental=${transformInvocation.isIncremental}" }
        val scanStart = System.currentTimeMillis()
        val transformInputs = transformInvocation.inputs
        val incremental = transformInvocation.isIncremental
        val outputProvider = transformInvocation.outputProvider

        if (!incremental) {
            outputProvider.deleteAll()
        }

        transformInputs.forEach { transformInput ->
            transformInput.jarInputs.forEach { jarInput ->
                Logger.d(TAG) { "JarInput: ${jarInput.file.absoluteFile}" }
                val dstFile = outputProvider.getContentLocation(
                    jarInput.name.computeDstFileName(),
                    jarInput.contentTypes,
                    jarInput.scopes,
                    Format.JAR
                )
                if (incremental && jarInput.status == Status.REMOVED) {
                    dstFile.deleteOnExit()
                } else {
                    jarInput.file.safelyCopyFile(dstFile)
                    targetClassCollector.collectForJarInput(jarInput)
                }
            }
            transformInput.directoryInputs.forEach { directoryInput ->
                Logger.d(TAG) { "DirectoryInput: ${directoryInput.file.absoluteFile}" }
                val dstDirectory = outputProvider.getContentLocation(
                    directoryInput.name.computeDstFileName(),
                    directoryInput.contentTypes,
                    directoryInput.scopes,
                    Format.DIRECTORY
                )
                if (incremental) {
                    directoryInput.changedFiles.filter { (file, _) ->
                        file.isClassFile()
                    }.forEach { (classFile, status) ->
                        val className = classFile.relativeFile(directoryInput.file).path
                        val dstFile = File(
                            dstDirectory,
                            className.computeDstFileName() + CLASS_FILE_SUFFIX
                        )
                        if (status == Status.REMOVED) {
                            dstFile.safelyDelete()
                        } else {
                            classFile.safelyCopyFile(dstFile)
                            targetClassCollector.collectForClassFile(
                                className,
                                classFile.inputStream()
                            )
                        }
                    }
                } else {
                    directoryInput.file.walkTopDown().filter {
                        it.isClassFile()
                    }.forEach { classFile ->
                        val className = classFile.relativeFile(directoryInput.file).path
                        Logger.d(TAG) { "className=$className" }
                        val dstFile = File(
                            dstDirectory,
                            className.computeDstFileName() + CLASS_FILE_SUFFIX
                        )
                        classFile.safelyCopyFile(dstFile)
                        targetClassCollector.collectForClassFile(className, classFile.inputStream())
                    }
                }
            }
        }
        val scanEnd = System.currentTimeMillis()
        Logger.i(TAG) { "onReceiveInput() >>> scan end, time cost: [${scanEnd - scanStart}ms]" }
    }

    private fun String.computeDstFileName(): String {
        return DigestUtils.md5Hex(this)
    }

    private fun computeServiceImplRelationship(): Map<ServiceInfo, List<ServiceImplInfo>> {
        val serviceInfoList = targetClassCollector.serviceInfoList
        val serviceImplInfoList = targetClassCollector.serviceImplInfoList
        return serviceInfoList.mapNotNull { serviceInfo ->
            val serviceInfoImpls = serviceImplInfoList.filter {
                it.implements.contains(serviceInfo.className)
            }
            if (serviceInfoImpls.isNotEmpty()) {
                serviceInfo to serviceInfoImpls
            } else {
                null
            }
        }.toMap()
    }
}