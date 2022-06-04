package com.spirytusz.spi.weaver.transform.scan

import com.android.build.api.transform.JarInput
import com.spirytusz.spi.weaver.config.FileConst.CLASS_FILE_SUFFIX
import com.spirytusz.spi.weaver.config.QualifiedNames.SERVICE_ANNOTATION_QUALIFIED_NAME
import com.spirytusz.spi.weaver.config.QualifiedNames.SERVICE_IMPL_ANNOTATION_QUALIFIED_NAME
import com.spirytusz.spi.weaver.extensions.toQualifiedName
import com.spirytusz.spi.weaver.transform.data.ServiceImplInfo
import com.spirytusz.spi.weaver.transform.data.ServiceInfo
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import java.io.InputStream
import java.util.jar.JarFile

class TargetClassCollector(private val classFilter: ClassFilter) {

    companion object {
        private const val SERVICE_IMPL_ALIAS = "alias"
    }

    private val classVisitor = ScanClassVisitor()

    fun collectForJarInput(
        jarInput: JarInput
    ): List<Any> {
        return JarFile(jarInput.file).use { jarFile ->
            jarFile.entries().toList().mapNotNull loop@{ jarEntry ->
                if (!jarEntry.name.endsWith(CLASS_FILE_SUFFIX)) {
                    return@loop null
                } else {
                    collectForClassFile(
                        jarEntry.name,
                        jarFile.getInputStream(jarEntry)
                    )
                }
            }
        }
    }

    fun collectForClassFile(
        className: String,
        inputStream: InputStream
    ): Any? {
        val clazz = if (className.endsWith(CLASS_FILE_SUFFIX)) {
            className.replace(CLASS_FILE_SUFFIX, "")
        } else {
            className
        }
        if (!classFilter(clazz)) {
            return null
        }
        classVisitor.reset()

        val classReader = ClassReader(inputStream)
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        return processScanResult(clazz)
    }

    private fun processScanResult(className: String): Any? {
        return when {
            classVisitor.isServiceClass -> {
                ServiceInfo(className = className)
            }
            classVisitor.isServiceImplClass -> {
                ServiceImplInfo(
                    implements = classVisitor.implements!!,
                    alias = classVisitor.serviceImplAlias!!,
                    className = className
                )
            }
            else -> null
        }
    }

    private inner class ScanClassVisitor : ClassVisitor(Opcodes.ASM6) {

        var isServiceClass = false
        var isServiceImplClass = false
        var serviceImplAlias: String? = null
        var implements: List<String>? = null

        fun reset() {
            isServiceClass = false
            isServiceImplClass = false
            serviceImplAlias = null
            implements = null
        }

        override fun visitAnnotation(desc: String?, visible: Boolean): AnnotationVisitor? {
            when (desc?.toQualifiedName()) {
                SERVICE_ANNOTATION_QUALIFIED_NAME -> {
                    isServiceClass = true
                }
                SERVICE_IMPL_ANNOTATION_QUALIFIED_NAME -> {
                    isServiceImplClass = true
                    return ScanAnnotationVisitor()
                }
            }
            return super.visitAnnotation(desc, visible)
        }

        override fun visit(
            version: Int,
            access: Int,
            name: String?,
            signature: String?,
            superName: String?,
            interfaces: Array<out String>?
        ) {
            implements = interfaces?.toList()
            super.visit(version, access, name, signature, superName, interfaces)
        }

        private inner class ScanAnnotationVisitor : AnnotationVisitor(Opcodes.ASM6) {

            override fun visit(name: String?, value: Any?) {
                if (name == SERVICE_IMPL_ALIAS) {
                    serviceImplAlias = value?.toString()
                }
                super.visit(name, value)
            }
        }
    }
}