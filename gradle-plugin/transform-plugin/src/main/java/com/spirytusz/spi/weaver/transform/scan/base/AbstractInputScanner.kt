package com.spirytusz.spi.weaver.transform.scan.base

import com.spirytusz.spi.weaver.transform.data.ServiceImplInfo
import com.spirytusz.spi.weaver.transform.data.ServiceInfo
import org.apache.commons.codec.digest.DigestUtils

abstract class AbstractInputScanner : IInputScanner {
    override val serviceMapping: Map<ServiceInfo, List<ServiceImplInfo>>
        get() = computeServiceMapping()

    protected val serviceInfoList = mutableListOf<ServiceInfo>()
    protected val serviceImplInfoList = mutableListOf<ServiceImplInfo>()

    protected fun String.computeDstFileName(): String {
        return DigestUtils.md5Hex(this)
    }

    private fun computeServiceMapping(): Map<ServiceInfo, List<ServiceImplInfo>> {
        return serviceInfoList.mapNotNull { serviceInfo ->
            val serviceInfoImpls = serviceImplInfoList.filter {
                it.implements.contains(serviceInfo.className)
            }
            if (serviceInfoImpls.isNotEmpty()) {
                serviceInfo to serviceInfoImpls
            } else {
                null
            }
        }.toMap()
    }
}