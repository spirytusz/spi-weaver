package com.spirytusz.spi.weaver.config

open class ServiceProviderExtension {

    var debuggable: Boolean = false

    var blackListClasses: List<String> = listOf()

    var whiteListClasses: List<String> = listOf()
}