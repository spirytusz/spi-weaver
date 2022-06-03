package com.spirytusz.spi.weaver.runtime

import com.spirytusz.spi.weaver.runtime.log.ServiceProviderLogger
import java.util.concurrent.ConcurrentHashMap

internal class ServiceHolder {

    companion object {
        private const val TAG = "ServiceHolder"
    }

    private val serviceImplByService: ConcurrentHashMap<Class<*>, ConcurrentHashMap<String, Any>> =
        ConcurrentHashMap()

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> query(alias: String, clazz: Class<T>): T? {
        val serviceImplByAlias = serviceImplByService[clazz] ?: run {
            ServiceProviderLogger.d(TAG, "query() >>> cache miss alias=$alias, service=$clazz")
            return null
        }
        ServiceProviderLogger.d(TAG, "query() >>> cache hit alias=$alias, service=$clazz")
        return serviceImplByAlias[alias] as? T
    }

    fun <T : Any> insert(alias: String, clazz: Class<T>, instance: T) {
        ServiceProviderLogger.d(TAG, "insert() >>> update cache alias=$alias clazz=$clazz")
        var serviceImplByAlias = serviceImplByService[clazz]
        if (serviceImplByAlias == null) {
            serviceImplByAlias = ConcurrentHashMap<String, Any>()
            serviceImplByService[clazz] = serviceImplByAlias
        }
        serviceImplByAlias[alias] = instance as Any
    }
}