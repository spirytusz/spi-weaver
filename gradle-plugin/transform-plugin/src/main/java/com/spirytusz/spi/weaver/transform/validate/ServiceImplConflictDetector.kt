package com.spirytusz.spi.weaver.transform.validate

import com.spirytusz.spi.weaver.log.Logger
import com.spirytusz.spi.weaver.transform.data.ServiceImplInfo
import com.spirytusz.spi.weaver.transform.data.ServiceInfo

class ServiceImplConflictDetector(
    private val serviceMapping: Map<ServiceInfo, List<ServiceImplInfo>>
) {

    companion object {
        private const val TAG = "ServiceImplConflictDetector"
    }

    fun checkOrThrow() {
        detectAliasConflict()
    }

    private fun detectAliasConflict() {
        val aliasConflicts = serviceMapping.values.map { impls ->
            val serviceImplsByAlias = impls.groupBy { it.alias }.values
            serviceImplsByAlias.filter {
                it.size >= 2
            }.flatten()
        }.filter {
            it.isNotEmpty()
        }
        aliasConflicts.forEach { impls ->
            val implNames = impls.map { it.className }
            val aliasName = impls.first().alias.ifEmpty { "NO_SPECIFIED_ALIAS" }
            Logger.e(TAG) {
                "conflict alias detect: alias=[$aliasName], services=$implNames"
            }
        }
    }
}