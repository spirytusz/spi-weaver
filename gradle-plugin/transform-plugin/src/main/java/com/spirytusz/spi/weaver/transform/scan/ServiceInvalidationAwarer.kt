package com.spirytusz.spi.weaver.transform.scan

import com.spirytusz.spi.weaver.log.Logger
import com.spirytusz.spi.weaver.transform.data.Cache

class ServiceInvalidationAwarer : (String, Cache) -> Unit {

    companion object {
        private const val TAG = "ServiceInvalidationAwarer"
    }

    private val changedTargetClasses = mutableSetOf<String>()

    val anyTargetClassInvalid: Boolean
        get() = changedTargetClasses.isNotEmpty()

    fun needReGenerate(className: String): Boolean {
        return changedTargetClasses.any { it == className }
    }

    override fun invoke(path: String, cache: Cache) {
        Logger.i(TAG) { "onInvalidateCache() >>> \n${cache.simpleInfo()}" }

        cache.serviceInfoList.forEach {
            changedTargetClasses.add(it.className)
        }
        cache.serviceImplInfoList.forEach {
            changedTargetClasses.add(it.className)
        }
    }

    private fun Cache.simpleInfo() = buildString {
        val cache = this@simpleInfo
        if (cache.serviceInfoList.isNotEmpty()) {
            append("service:\n")
            cache.serviceInfoList.forEach {
                append("${it.className}\n")
            }
        }
        if (cache.serviceImplInfoList.isNotEmpty()) {
            append("impl:\n")
            cache.serviceImplInfoList.forEach {
                append("[${it.alias}] ${it.className}\n")
            }
        }
    }
}