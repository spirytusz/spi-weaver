package com.spirytusz.spi.weaver.extensions

import java.io.File

fun String.toQualifiedName(): String {
    return removePrefix("L").removeSuffix(";").replace(File.separator, ".")
}