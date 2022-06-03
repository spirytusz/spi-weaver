package com.spirytusz.spi.weaver.runtime

import com.spirytusz.spi.weaver.runtime.annotation.NO_SPECIFIED_ALIAS
import com.spirytusz.spi.weaver.runtime.log.LogProxy
import com.spirytusz.spi.weaver.runtime.log.ServiceProviderLogger

@Suppress("UNCHECKED_CAST")
object ServiceProvider {

    private const val TAG = "ServiceProvider"

    private val serviceHolder by lazy { ServiceHolder() }

    fun setLogger(logProxy: LogProxy) {
        ServiceProviderLogger.logProxy = logProxy
    }

    @JvmStatic
    fun <T : Any> of(clazz: Class<T>): T? {
        return of(NO_SPECIFIED_ALIAS, clazz)
    }

    @JvmStatic
    fun <T : Any> of(alias: String, clazz: Class<T>): T? {
        val cache = serviceHolder.query(alias, clazz)
        if (cache != null) {
            return cache
        }

        val servicePool = sServicesPool
        val serviceImpls = servicePool[clazz]
        if (!servicePool.containsKey(clazz) || serviceImpls == null) {
            ServiceProviderLogger.w(TAG, "not found any service $clazz")
            return null
        }

        if (serviceImpls.size > 1 && alias == NO_SPECIFIED_ALIAS) {
            throw IllegalArgumentException("no specify alias but has ${serviceImpls.size} impls?")
        }
        val serviceImpl = serviceImpls[alias]?.call() as? T
        if (serviceImpl != null) {
            serviceHolder.insert(alias, clazz, serviceImpl)
        }
        return serviceImpl
    }
}