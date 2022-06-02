package com.spirytusz.spi.weaver.transform.extensions

import com.spirytusz.spi.weaver.config.FileConst.CLASS_FILE_SUFFIX
import com.spirytusz.spi.weaver.log.Logger
import org.apache.commons.io.FileUtils
import java.io.File

fun File.relativeFile(base: File): File {
    val basePath = if (base.absolutePath.endsWith(File.separator)) {
        base.absolutePath
    } else {
        base.absolutePath + File.separator
    }
    return File(absolutePath.replace(basePath, ""))
}

fun File.safelyDelete() {
    runCatching { FileUtils.forceDelete(this) }
}

fun File.safelyCopyFile(dst: File) {
    runCatching { FileUtils.copyFile(this, dst) }
}

fun File.safelyWriteBytes(byteArray: ByteArray) {
    runCatching {
        FileUtils.writeByteArrayToFile(
            this,
            byteArray
        )
    }.onFailure { Logger.e("FileExtensions") { "safelyWriteBytes() failed $it" } }
}

fun File.isClassFile(): Boolean = !isDirectory && path.endsWith(CLASS_FILE_SUFFIX)