package com.spirytusz.spi.weaver.transform.scan

import com.android.build.api.transform.TransformInvocation
import com.spirytusz.spi.weaver.log.Logger
import com.spirytusz.spi.weaver.transform.cache.CacheManager
import com.spirytusz.spi.weaver.transform.data.ServiceImplInfo
import com.spirytusz.spi.weaver.transform.data.ServiceInfo
import com.spirytusz.spi.weaver.transform.global.TransformContext
import com.spirytusz.spi.weaver.transform.scan.base.IInputScanner
import com.spirytusz.spi.weaver.transform.scan.base.InputScannerDispatcher
import com.spirytusz.spi.weaver.transform.scan.directory.FullDirectoryInputScanner
import com.spirytusz.spi.weaver.transform.scan.directory.IncrementalDirectoryInputScanner
import com.spirytusz.spi.weaver.transform.scan.jar.FullJarInputScanner
import com.spirytusz.spi.weaver.transform.scan.jar.IncrementalJarInputScanner

class InputScanner(
    transformContext: TransformContext,
    private val serviceInvalidationAwarer: ServiceInvalidationAwarer
) : IInputScanner {

    companion object {
        private const val TAG = "InputScanner"
    }

    private val targetClassCollector =
        TargetClassCollector(ClassFilter(transformContext.configProvider))
    private val cacheHelper = CacheManager(transformContext).also {
        it.onInvalidateListener = serviceInvalidationAwarer
    }

    private val jarInputScanner by lazy {
        InputScannerDispatcher(
            incrementalScanner = IncrementalJarInputScanner(targetClassCollector, cacheHelper),
            fullScanner = FullJarInputScanner(targetClassCollector, cacheHelper)
        )
    }

    private val directoryInputScanner by lazy {
        InputScannerDispatcher(
            incrementalScanner = IncrementalDirectoryInputScanner(
                targetClassCollector,
                cacheHelper
            ),
            fullScanner = FullDirectoryInputScanner(targetClassCollector, cacheHelper)
        )
    }

    override val serviceMapping: Map<ServiceInfo, List<ServiceImplInfo>>
        get() = mergeServiceMapping()

    override fun onReceiveInput(transformInvocation: TransformInvocation) {
        Logger.i(TAG) {
            "onReceiveInput() >>> scan start, isIncremental=${transformInvocation.isIncremental}"
        }
        val scanStart = System.currentTimeMillis()
        cacheHelper.init(transformInvocation)
        val incremental = transformInvocation.isIncremental
        val outputProvider = transformInvocation.outputProvider

        if (!incremental) {
            outputProvider.deleteAll()
        }

        jarInputScanner.onReceiveInput(transformInvocation)
        directoryInputScanner.onReceiveInput(transformInvocation)

        cacheHelper.apply()

        val scanEnd = System.currentTimeMillis()
        Logger.i(TAG) {
            "onReceiveInput() >>> scan end, summary:" +
                    "\ntimeCost: [${scanEnd - scanStart}ms]" +
                    "\nclasses:" +
                    "\n${makeScanSummary(transformInvocation)}"
        }
    }

    private fun mergeServiceMapping(): Map<ServiceInfo, List<ServiceImplInfo>> {
        val result = mutableMapOf<ServiceInfo, List<ServiceImplInfo>>()
        result.putAll(jarInputScanner.serviceMapping)
        result.putAll(directoryInputScanner.serviceMapping)
        return result.toMap()
    }

    private fun makeScanSummary(transformInvocation: TransformInvocation) = buildString {
        fun String.makePrefix(): String {
            return if (!transformInvocation.isIncremental) {
                "FULL"
            } else if (serviceInvalidationAwarer.needReGenerate(this)) {
                "CHANGED"
            } else {
                "DEFAULT"
            }
        }

        serviceMapping.forEach { (service, impls) ->
            val serviceName = service.className
            append("\n\t[service] (${serviceName.makePrefix()})$serviceName")
            impls.forEach { impl ->
                val implName = impl.className
                append("\n\t\t[impl] (${implName.makePrefix()}) alias: ${impl.alias} name: $implName")
            }
        }
    }.removePrefix("\n")
}