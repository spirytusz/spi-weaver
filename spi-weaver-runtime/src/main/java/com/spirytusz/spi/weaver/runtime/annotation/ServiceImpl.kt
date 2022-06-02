package com.spirytusz.spi.weaver.runtime.annotation

const val NO_SPECIFIED_ALIAS = ""

@Retention(AnnotationRetention.BINARY)
annotation class ServiceImpl(val alias: String = NO_SPECIFIED_ALIAS)
