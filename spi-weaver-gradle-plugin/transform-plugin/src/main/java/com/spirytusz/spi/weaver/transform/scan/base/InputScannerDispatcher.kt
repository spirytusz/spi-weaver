package com.spirytusz.spi.weaver.transform.scan.base

import com.android.build.api.transform.TransformInvocation
import com.spirytusz.spi.weaver.transform.data.ServiceImplInfo
import com.spirytusz.spi.weaver.transform.data.ServiceInfo

class InputScannerDispatcher(
    private val incrementalScanner: IInputScanner,
    private val fullScanner: IInputScanner
) : IInputScanner {

    private var incremental = false

    override val serviceMapping: Map<ServiceInfo, List<ServiceImplInfo>>
        get() = if (incremental) {
            incrementalScanner.serviceMapping
        } else {
            fullScanner.serviceMapping
        }

    override fun onReceiveInput(transformInvocation: TransformInvocation) {
        incremental = transformInvocation.isIncremental
        if (transformInvocation.isIncremental) {
            incrementalScanner.onReceiveInput(transformInvocation)
        } else {
            fullScanner.onReceiveInput(transformInvocation)
        }
    }
}