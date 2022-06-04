package com.spirytusz.spi.weaver.transform.scan.base

import com.android.build.api.transform.TransformInvocation
import com.spirytusz.spi.weaver.transform.data.ServiceImplInfo
import com.spirytusz.spi.weaver.transform.data.ServiceInfo

interface IInputScanner {

    val serviceMapping: Map<ServiceInfo, List<ServiceImplInfo>>

    fun onReceiveInput(transformInvocation: TransformInvocation)
}