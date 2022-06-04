package com.spirytusz.spi.weaver.transform.scan

import com.android.build.api.transform.TransformInvocation
import com.spirytusz.spi.weaver.config.ConfigProvider
import com.spirytusz.spi.weaver.log.Logger
import com.spirytusz.spi.weaver.transform.data.ServiceImplInfo
import com.spirytusz.spi.weaver.transform.data.ServiceInfo
import com.spirytusz.spi.weaver.transform.scan.base.IInputScanner
import com.spirytusz.spi.weaver.transform.scan.base.InputScannerDispatcher
import com.spirytusz.spi.weaver.transform.scan.directory.FullDirectoryInputScanner
import com.spirytusz.spi.weaver.transform.scan.directory.IncrementalDirectoryInputScanner
import com.spirytusz.spi.weaver.transform.scan.jar.FullJarInputScanner
import com.spirytusz.spi.weaver.transform.scan.jar.IncrementalJarInputScanner
import org.gradle.api.Project

class InputScanner(private val project: Project, configProvider: ConfigProvider) : IInputScanner {

    companion object {
        private const val TAG = "InputScanner"
    }

    private val targetClassCollector = TargetClassCollector(ClassFilter(configProvider))

    private val jarInputScanner by lazy {
        InputScannerDispatcher(
            incrementalScanner = IncrementalJarInputScanner(targetClassCollector),
            fullScanner = FullJarInputScanner(targetClassCollector)
        )
    }

    private val directoryInputScanner by lazy {
        InputScannerDispatcher(
            incrementalScanner = IncrementalDirectoryInputScanner(targetClassCollector),
            fullScanner = FullDirectoryInputScanner(targetClassCollector)
        )
    }

    override val serviceMapping: Map<ServiceInfo, List<ServiceImplInfo>>
        get() = mergeServiceMapping()

    override fun onReceiveInput(transformInvocation: TransformInvocation) {
        Logger.i(TAG) {
            "onReceiveInput() >>> scan start, isIncremental=${transformInvocation.isIncremental}"
        }
        val scanStart = System.currentTimeMillis()
        val incremental = transformInvocation.isIncremental
        val outputProvider = transformInvocation.outputProvider

        if (!incremental) {
            outputProvider.deleteAll()
        }

        jarInputScanner.onReceiveInput(transformInvocation)
        directoryInputScanner.onReceiveInput(transformInvocation)

        val scanEnd = System.currentTimeMillis()
        Logger.i(TAG) { "onReceiveInput() >>> scan end, time cost: [${scanEnd - scanStart}ms]" }
    }

    private fun mergeServiceMapping(): Map<ServiceInfo, List<ServiceImplInfo>> {
        val result = mutableMapOf<ServiceInfo, List<ServiceImplInfo>>()
        result.putAll(jarInputScanner.serviceMapping)
        result.putAll(directoryInputScanner.serviceMapping)
        return result.toMap()
    }
}