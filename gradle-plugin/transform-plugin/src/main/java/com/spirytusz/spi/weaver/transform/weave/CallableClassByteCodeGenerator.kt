package com.spirytusz.spi.weaver.transform.weave

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes

object CallableClassByteCodeGenerator {

    private const val CREATOR_CLASS_SUFFIX = "_Creator"

    fun generate(className: String): Pair<String, ByteArray> {
        val creatorClassName = makeCreatorClassName(className)
        val classWriter = ClassWriter(0)
        classWriter.visit(
            Opcodes.V1_8,
            Opcodes.ACC_PUBLIC or Opcodes.ACC_SUPER,
            creatorClassName,
            "Ljava/lang/Object;Ljava/util/concurrent/Callable<L$className;>;",
            "java/lang/Object",
            arrayOf("java/util/concurrent/Callable")
        )
        visitConstructor(classWriter)
        visitCall(classWriter, className)
        visitCallSynthetic(classWriter, className, creatorClassName)
        classWriter.visitEnd()
        return creatorClassName to classWriter.toByteArray()
    }

    private fun visitConstructor(classWriter: ClassWriter) {
        val methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null)
        methodVisitor.visitCode()
        val label = Label()
        methodVisitor.visitLabel(label)
        methodVisitor.visitLineNumber(6, label)
        // aload_0
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0)
        // invokespecial java/lang/Object."<init>":()V
        methodVisitor.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            "java/lang/Object",
            "<init>",
            "()V",
            false
        )
        // return
        methodVisitor.visitInsn(Opcodes.RETURN)
        // stack=1, locals=1
        methodVisitor.visitMaxs(1, 1)
        methodVisitor.visitEnd()
    }

    private fun visitCall(classWriter: ClassWriter, className: String) {
        val methodVisitor = classWriter.visitMethod(
            Opcodes.ACC_PUBLIC,
            "call",
            "()L$className;",
            null,
            arrayOf("java/lang/Exception")
        )
        methodVisitor.visitCode()
        val label = Label()
        methodVisitor.visitLabel(label)
        methodVisitor.visitLineNumber(9, label)
        // new $className
        methodVisitor.visitTypeInsn(Opcodes.NEW, className)
        // dup
        methodVisitor.visitInsn(Opcodes.DUP)
        // invokespecial $className."<init>":()V
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, className, "<init>", "()V", false)
        // areturn
        methodVisitor.visitInsn(Opcodes.ARETURN)
        // stack=2, locals=1
        methodVisitor.visitMaxs(2, 1)
        methodVisitor.visitEnd()
    }

    private fun visitCallSynthetic(
        classWriter: ClassWriter,
        className: String,
        creatorClassName: String
    ) {
        val methodVisitor = classWriter.visitMethod(
            Opcodes.ACC_PUBLIC or Opcodes.ACC_SYNTHETIC or Opcodes.ACC_BRIDGE,
            "call",
            "()Ljava/lang/Object;",
            null,
            arrayOf("java/lang/Exception")
        )
        methodVisitor.visitCode()
        val label = Label()
        methodVisitor.visitLabel(label)
        methodVisitor.visitLineNumber(6, label)
        // aload_0
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0)
        // invokevirtual call:()L$className;
        methodVisitor.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            creatorClassName,
            "call",
            "()L$className;",
            false
        )
        // areturn
        methodVisitor.visitInsn(Opcodes.ARETURN)
        // stack=1, locals=1
        methodVisitor.visitMaxs(1, 1)
        methodVisitor.visitEnd()
    }

    private fun makeCreatorClassName(className: String): String {
        return className + CREATOR_CLASS_SUFFIX
    }
}