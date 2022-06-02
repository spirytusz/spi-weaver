package com.spirytusz.spi.weaver.transform.weave

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.TransformInvocation
import com.spirytusz.spi.weaver.config.FileConst
import com.spirytusz.spi.weaver.log.Logger
import com.spirytusz.spi.weaver.transform.data.ServiceImplInfo
import com.spirytusz.spi.weaver.transform.data.ServiceInfo
import com.spirytusz.spi.weaver.transform.extensions.safelyWriteBytes
import org.apache.commons.codec.digest.DigestUtils
import java.io.File

class CodeGenerator(
    private val transformInvocation: TransformInvocation,
    private val serviceMapping: Map<ServiceInfo, List<ServiceImplInfo>>
) {

    companion object {
        private const val TAG = "CodeGenerator"
    }

    fun generateServiceImplCreators() {
        serviceMapping.values.forEach { impls ->
            impls.forEach { impl ->
                val (creatorName, byteArray) = CallableClassByteCodeGenerator.generate(impl.className)
                val dir = transformInvocation.outputProvider.getContentLocation(
                    DigestUtils.md2Hex(impl.toString()) + FileConst.CLASS_FILE_SUFFIX,
                    mutableSetOf<QualifiedContent.ContentType>(QualifiedContent.DefaultContentType.CLASSES),
                    mutableSetOf(QualifiedContent.Scope.PROJECT),
                    Format.DIRECTORY
                )
                val file = File(dir, namingCreatorClassFile(creatorName))
                file.safelyWriteBytes(byteArray)
                Logger.i(TAG) { "transform() >>> write creator $creatorName to $file" }
            }
        }
    }

    fun generateServicePool() {

    }

    private fun namingCreatorClassFile(creatorName: String): String {
        val name = DigestUtils.md5Hex(creatorName.replace("/", "_"))
        return name + FileConst.CLASS_FILE_SUFFIX
    }
}