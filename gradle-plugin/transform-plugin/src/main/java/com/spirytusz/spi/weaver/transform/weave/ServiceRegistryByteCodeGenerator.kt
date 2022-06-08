package com.spirytusz.spi.weaver.transform.weave

import com.spirytusz.spi.weaver.transform.data.ServiceImplInfo
import com.spirytusz.spi.weaver.transform.data.ServiceInfo
import org.objectweb.asm.*

@Suppress("SameParameterValue")
object ServiceRegistryByteCodeGenerator {

    private const val SERVICE_REGISTRY_CLASS_NAME =
        "com/spirytusz/spi/weaver/runtime/ServiceRegistry"

    private const val SERVICE_POOL_FIELD_NAME = "sServicesPool"

    fun generate(serviceMapping: Map<ServiceInfo, List<ServiceImplInfo>>): Pair<String, ByteArray> {
        val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
        classWriter.visit(
            Opcodes.V1_8,
            Opcodes.ACC_PUBLIC or Opcodes.ACC_SUPER,
            SERVICE_REGISTRY_CLASS_NAME,
            null,
            "java/lang/Object",
            arrayOf()
        )
        visitServicePoolField(classWriter)
        visitConstructor(classWriter)
        visitServicePoolGetter(classWriter)
        visitStaticBlock(classWriter, serviceMapping)
        classWriter.visitEnd()
        return SERVICE_REGISTRY_CLASS_NAME to classWriter.toByteArray()
    }

    private fun visitServicePoolField(classWriter: ClassWriter) {
        val fieldVisitor = classWriter.visitField(
            Opcodes.ACC_PRIVATE or Opcodes.ACC_STATIC or Opcodes.ACC_FINAL,
            SERVICE_POOL_FIELD_NAME,
            "Ljava/util/Map;",
            "Ljava/util/Map<Ljava/lang/Class<*>;Ljava/util/Map<Ljava/lang/String;Ljava/util/concurrent/Callable<*>;>;>;",
            null
        )
        fieldVisitor.visitEnd()
    }

    private fun visitConstructor(classWriter: ClassWriter) {
        val methodVisitor = classWriter.visitMethod(
            Opcodes.ACC_PUBLIC,
            "<init>",
            "()V",
            null,
            null
        )
        methodVisitor.visitCode()
        val label = Label()
        methodVisitor.visitLabel(label)
        methodVisitor.visitLineNumber(7, label)
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0)
        methodVisitor.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            "java/lang/Object",
            "<init>",
            "()V",
            false
        )
        methodVisitor.visitInsn(Opcodes.RETURN)
        methodVisitor.visitMaxs(0, 0)
        methodVisitor.visitEnd()
    }

    private fun visitServicePoolGetter(classWriter: ClassWriter) {
        val methodName = "get" + SERVICE_POOL_FIELD_NAME.let {
            val firstChar = it.first()
            it.replaceFirst(firstChar, firstChar.uppercaseChar())
        }
        val methodVisitor = classWriter.visitMethod(
            Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC,
            methodName,
            "()Ljava/util/Map;",
            "()Ljava/util/Map<Ljava/lang/Class<*>;Ljava/util/Map<Ljava/lang/String;Ljava/util/concurrent/Callable<*>;>;>;",
            null
        )
        methodVisitor.visitCode()
        val label = Label()
        methodVisitor.visitLabel(label)
        methodVisitor.visitLineNumber(12, label)
        methodVisitor.visitFieldInsn(
            Opcodes.GETSTATIC,
            SERVICE_REGISTRY_CLASS_NAME,
            SERVICE_POOL_FIELD_NAME,
            "Ljava/util/Map;"
        )
        methodVisitor.visitInsn(Opcodes.ARETURN)
        methodVisitor.visitMaxs(0, 0)
        methodVisitor.visitEnd()
    }

    private fun visitStaticBlock(
        classWriter: ClassWriter,
        serviceMapping: Map<ServiceInfo, List<ServiceImplInfo>>
    ) {
        val methodVisitor = classWriter.visitMethod(
            Opcodes.ACC_STATIC,
            "<clinit>",
            "()V",
            null,
            null
        )
        methodVisitor.visitCode()
        val label = Label()
        methodVisitor.visitLabel(label)
        methodVisitor.visitLineNumber(16, label)
        methodVisitor.visitTypeInsn(Opcodes.NEW, "java/util/concurrent/ConcurrentHashMap")
        methodVisitor.visitInsn(Opcodes.DUP)
        methodVisitor.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            "java/util/concurrent/ConcurrentHashMap",
            "<init>",
            "()V",
            false
        )
        methodVisitor.visitFieldInsn(
            Opcodes.PUTSTATIC,
            SERVICE_REGISTRY_CLASS_NAME,
            SERVICE_POOL_FIELD_NAME,
            "Ljava/util/Map;"
        )

        var lineNumberOffset = 0
        serviceMapping.forEach { (serviceInfo, serviceImpls) ->
            lineNumberOffset += registerService(methodVisitor, serviceInfo, serviceImpls, 17)
        }

        val label2 = Label()
        methodVisitor.visitLabel(label2)
        methodVisitor.visitLineNumber(lineNumberOffset + 17, label2)
        methodVisitor.visitInsn(Opcodes.RETURN)
        methodVisitor.visitMaxs(0, 0)
        methodVisitor.visitEnd()
    }

    private fun registerService(
        methodVisitor: MethodVisitor,
        serviceInfo: ServiceInfo,
        serviceImpls: List<ServiceImplInfo>,
        startLineNumber: Int
    ): Int {

        var lineNumber = startLineNumber
        fun declareLocalVariable(type: String) {
            val label = Label()
            methodVisitor.visitLabel(label)
            methodVisitor.visitLineNumber(lineNumber++, label)
            methodVisitor.visitLdcInsn(Type.getObjectType(type))
            methodVisitor.visitVarInsn(Opcodes.ASTORE, 0)
        }

        fun declareServiceImplMap(serviceImpls: List<ServiceImplInfo>) {
            val label1 = Label()
            methodVisitor.visitLabel(label1)
            methodVisitor.visitLineNumber(lineNumber++, label1)
            methodVisitor.visitTypeInsn(Opcodes.NEW, "java/util/concurrent/ConcurrentHashMap")
            methodVisitor.visitInsn(Opcodes.DUP)
            methodVisitor.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                "java/util/concurrent/ConcurrentHashMap",
                "<init>",
                "()V",
                false
            )
            methodVisitor.visitVarInsn(Opcodes.ASTORE, 1)
            serviceImpls.forEach { serviceImpl ->
                val label2 = Label()
                val creatorName = namingCreator(serviceImpl.className)
                methodVisitor.visitLabel(label2)
                methodVisitor.visitLineNumber(lineNumber++, label2)
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 1)
                methodVisitor.visitLdcInsn(serviceImpl.alias)
                methodVisitor.visitTypeInsn(Opcodes.NEW, creatorName)
                methodVisitor.visitInsn(Opcodes.DUP)
                methodVisitor.visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    creatorName,
                    "<init>",
                    "()V",
                    false
                )
                methodVisitor.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE,
                    "java/util/Map",
                    "put",
                    "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                    true
                )
                methodVisitor.visitInsn(Opcodes.POP)
            }
        }

        fun serviceRegistry() {
            val label = Label()
            methodVisitor.visitLabel(label)
            methodVisitor.visitLineNumber(lineNumber++, label)
            methodVisitor.visitFieldInsn(
                Opcodes.GETSTATIC,
                SERVICE_REGISTRY_CLASS_NAME,
                SERVICE_POOL_FIELD_NAME,
                "Ljava/util/Map;"
            )
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0)
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 1)
            methodVisitor.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                "java/util/Map",
                "put",
                "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                true
            )
            methodVisitor.visitInsn(Opcodes.POP)
        }

        declareLocalVariable(serviceInfo.className)
        declareServiceImplMap(serviceImpls)
        serviceRegistry()
        return lineNumber - startLineNumber
    }

    private fun namingCreator(className: String): String {
        return className + "_Creator"
    }
}