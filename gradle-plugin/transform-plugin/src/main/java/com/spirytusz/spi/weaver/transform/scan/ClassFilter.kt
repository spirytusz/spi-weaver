package com.spirytusz.spi.weaver.transform.scan

import com.spirytusz.spi.weaver.config.ConfigProvider

class ClassFilter(configProvider: ConfigProvider) : (String) -> Boolean {

    private val blacklist = configProvider.blackList

    override fun invoke(className: String): Boolean {
        return blacklist.none { it.matcher(className).find() }
    }
}