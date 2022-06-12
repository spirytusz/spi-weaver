package com.spirytusz.spi.weaver.transform.weave

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.TransformInvocation
import com.spirytusz.spi.weaver.config.FileConst
import com.spirytusz.spi.weaver.log.Logger
import com.spirytusz.spi.weaver.transform.data.ServiceImplInfo
import com.spirytusz.spi.weaver.transform.data.ServiceInfo
import com.spirytusz.spi.weaver.transform.extensions.safelyWriteBytes
import com.spirytusz.spi.weaver.transform.scan.ServiceInvalidationAwarer
import org.apache.commons.codec.digest.DigestUtils
import java.io.File

class CodeGenerator(
    private val transformInvocation: TransformInvocation,
    private val serviceMapping: Map<ServiceInfo, List<ServiceImplInfo>>,
    private val invalidationAwarer: ServiceInvalidationAwarer
) {

    companion object {
        private const val TAG = "CodeGenerator"
    }

    fun generate() {
        val generateStart = System.currentTimeMillis()
        val creatorNames = generateServiceImplCreators()
        // 增量编译 且 没有任何关心的类被修改
        val skipGenServiceRegistry = transformInvocation.isIncremental
                && !invalidationAwarer.anyTargetClassInvalid
        if (!skipGenServiceRegistry) {
            generateServiceRegistry()
        }
        val generateEnd = System.currentTimeMillis()
        Logger.i(TAG) {
            val timeCost = generateEnd - generateStart
            val summary = buildString {
                append("\n\ttimeCost: [${timeCost}ms]")
                append("\n\tneedGenServiceRegistry=${!skipGenServiceRegistry}")
                if (creatorNames.isNotEmpty()) {
                    val joinName = creatorNames.joinToString(separator = "\n\t\t") { it }
                    append("\n\tneedGenCreatorNames=$joinName")
                } else {
                    append("\n\tcreatorNames no need gen")
                }
            }
            "generate() >>> generate end, summary: $summary"
        }
    }

    private fun generateServiceImplCreators(): List<String> {
        val incremental = transformInvocation.isIncremental
        return serviceMapping.values.map { impls ->
            impls.filter {
                !incremental || invalidationAwarer.needReGenerate(it.className)
            }.map { impl ->
                val (creatorName, byteArray) = CallableClassByteCodeGenerator.generate(impl.className)
                val dir = transformInvocation.outputProvider.getContentLocation(
                    DigestUtils.md5Hex(impl.toString()) + FileConst.CLASS_FILE_SUFFIX,
                    mutableSetOf<QualifiedContent.ContentType>(QualifiedContent.DefaultContentType.CLASSES),
                    mutableSetOf(QualifiedContent.Scope.PROJECT),
                    Format.DIRECTORY
                )
                val file = File(dir, namingClassFile(creatorName))
                file.safelyWriteBytes(byteArray)
                creatorName
            }
        }.flatten()
    }

    private fun generateServiceRegistry() {
        val (className, byteArray) = ServiceRegistryByteCodeGenerator.generate(serviceMapping)
        val dir = transformInvocation.outputProvider.getContentLocation(
            DigestUtils.md5Hex(className) + FileConst.CLASS_FILE_SUFFIX,
            mutableSetOf<QualifiedContent.ContentType>(QualifiedContent.DefaultContentType.CLASSES),
            mutableSetOf(QualifiedContent.Scope.PROJECT),
            Format.DIRECTORY
        )
        val file = File(dir, namingClassFile(className))
        file.safelyWriteBytes(byteArray)
        Logger.i(TAG) { "generateServiceRegistry() >>> write $className to $file" }
    }

    private fun namingClassFile(className: String): String {
        val name = DigestUtils.md5Hex(className.replace("/", "_"))
        return name + FileConst.CLASS_FILE_SUFFIX
    }
}