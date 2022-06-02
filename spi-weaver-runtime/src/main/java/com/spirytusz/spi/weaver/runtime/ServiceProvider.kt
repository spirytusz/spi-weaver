package com.spirytusz.spi.weaver.runtime


import com.spirytusz.spi.weaver.runtime.annotation.NO_SPECIFIED_ALIAS
import com.spirytusz.spi.weaver.runtime.log.LogProxy
import java.util.concurrent.ConcurrentHashMap

@Suppress("UNCHECKED_CAST")
object ServiceProvider {

    private const val TAG = "ServiceProvider"

    var logProxy: LogProxy? = null

    private val serviceImplByService: ConcurrentHashMap<Class<*>, ConcurrentHashMap<String, Any>> =
        ConcurrentHashMap()

    @JvmStatic
    fun <T : Any> of(clazz: Class<T>): T? {
        return of(NO_SPECIFIED_ALIAS, clazz)
    }

    fun <T : Any> of(alias: String, clazz: Class<T>): T? {
        val cache = getCache(clazz, alias)
        if (cache != null) {
            return cache
        }
        val serviceImpl = retrieveFromServicePool(clazz, alias) ?: return null
        var serviceImplByAlias = serviceImplByService[clazz]
        if (serviceImplByAlias != null) {
            serviceImplByAlias[alias] = serviceImpl
        } else {
            serviceImplByAlias = ConcurrentHashMap()
            serviceImplByAlias[alias] = serviceImpl
            serviceImplByService[clazz] = serviceImplByAlias
        }
        return serviceImpl
    }

    private fun <T : Any> getCache(clazz: Class<T>, alias: String = NO_SPECIFIED_ALIAS): T? {
        return serviceImplByService.search("getCache", clazz, alias) as? T
    }

    private fun <T : Any> retrieveFromServicePool(
        clazz: Class<T>,
        alias: String = NO_SPECIFIED_ALIAS
    ): T? = synchronized(this) {
        sServicesPool.search("retrieveFromServicePool", clazz, alias)?.call() as? T
    }

    private fun <T : Any> Map<Class<*>, Map<String, T>>.search(
        logPrefix: String,
        clazz: Class<*>,
        alias: String = NO_SPECIFIED_ALIAS
    ): T? {
        if (!containsKey(clazz)) {
            logProxy?.d(TAG, "$logPrefix >>> no impl for class [$clazz]")
            return null
        }
        val tByAlias = this[clazz]
        if (tByAlias == null || tByAlias.isEmpty()) {
            logProxy?.d(TAG, "$logPrefix >>> no impl for class [$clazz]")
            return null
        }
        if (!tByAlias.containsKey(alias)) {
            logProxy?.d(TAG, "$logPrefix >>> no alias: [$alias] for class [$clazz]")
            return null
        }
        return tByAlias[alias]
    }
}