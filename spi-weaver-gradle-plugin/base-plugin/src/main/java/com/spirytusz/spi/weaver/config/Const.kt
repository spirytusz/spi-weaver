package com.spirytusz.spi.weaver.config

object QualifiedNames {
    const val SERVICE_ANNOTATION_QUALIFIED_NAME =
        "com.spirytusz.spi.weaver.runtime.annotation.Service"

    const val SERVICE_IMPL_ANNOTATION_QUALIFIED_NAME =
        "com.spirytusz.spi.weaver.runtime.annotation.ServiceImpl"
}

object Caches {
    const val INTERMEDIATES = "intermediates"
    const val CACHE_FOLDER = "spi-weaver"
    const val CACHE_FILE_NAME = "spi-weaver-cache.json"
}

object FileConst {
    const val CLASS_FILE_SUFFIX = ".class"
    const val JAR_FILE_SUFFIX = ".jar"
}