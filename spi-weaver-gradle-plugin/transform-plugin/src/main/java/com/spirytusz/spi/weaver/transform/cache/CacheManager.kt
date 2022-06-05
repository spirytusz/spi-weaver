package com.spirytusz.spi.weaver.transform.cache

import com.android.build.api.transform.TransformInvocation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.spirytusz.spi.weaver.config.Caches.CACHE_FILE_NAME
import com.spirytusz.spi.weaver.config.Caches.CACHE_FOLDER
import com.spirytusz.spi.weaver.config.Caches.INTERMEDIATES
import com.spirytusz.spi.weaver.log.Logger
import com.spirytusz.spi.weaver.transform.data.Cache
import com.spirytusz.spi.weaver.transform.data.ServiceImplInfo
import com.spirytusz.spi.weaver.transform.data.ServiceInfo
import org.gradle.api.Project
import java.io.File

class CacheManager(private val project: Project) {

    companion object {
        private const val TAG = "CacheHelper"
        private const val DEFAULT_CACHE = "{}"
    }

    private val cacheMapping: MutableMap<String, Cache> = mutableMapOf()
    private val cacheFile by lazy { getCacheJsonFile() }
    private val gson = Gson()

    fun init(transformInvocation: TransformInvocation) {
        Logger.i(TAG) { "init() >>> isIncremental=${transformInvocation.isIncremental}" }
        if (transformInvocation.isIncremental) {
            val json = cacheFile.readText().ifEmpty { DEFAULT_CACHE }
            val type = object : TypeToken<Map<String, Cache>>() {}.type
            val cacheMapping = gson.fromJson<Map<String, Cache>>(json, type)
            this.cacheMapping.putAll(cacheMapping)
        } else {
            cacheMapping.clear()
        }
    }

    fun findByPath(path: String): Cache? {
        val cache = cacheMapping[path]
        if (cache != null) {
            Logger.i(TAG) { "findByPath() >>> cache hit $path -> $cache" }
        } else {
            Logger.d(TAG) { "findByPath() >>> cache miss" }
        }
        return cache
    }

    fun insertByPath(
        path: String,
        service: ServiceInfo? = null,
        impl: ServiceImplInfo? = null
    ) {
        if (service == null && impl == null) {
            Logger.e(TAG) { "insertByPath() >>> both service and impl is null!" }
            return
        }
        service?.let { Logger.i(TAG) { "insertByPath() >>> $path -> $it" } }
        impl?.let { Logger.i(TAG) { "insertByPath() >>> $path -> $it" } }
        val existOne = cacheMapping[path]
        val services = service?.let { setOf(it) } ?: emptySet()
        val impls = impl?.let { setOf(it) } ?: emptySet()
        val newCache = Cache(
            serviceInfoList = services + (existOne?.serviceInfoList ?: emptySet()),
            serviceImplInfoList = impls + (existOne?.serviceImplInfoList ?: emptySet())
        )
        cacheMapping[path] = newCache
    }

    fun invalidateByPath(path: String) {
        Logger.i(TAG) { "invalidateByPath() >>> $path" }
        cacheMapping.remove(path)
    }

    fun apply() {
        val json = gson.toJson(cacheMapping)
        cacheFile.writeText(json)
        Logger.i(TAG) { "apply() >>> $json" }
    }

    private fun getCacheJsonFile(): File {
        val intermediates = File(project.buildDir, INTERMEDIATES)
        val cacheFolder = File(intermediates, CACHE_FOLDER)
        if (!cacheFolder.exists()) {
            cacheFolder.mkdir()
        }
        val cacheFile = File(cacheFolder, CACHE_FILE_NAME)
        if (!cacheFile.exists()) {
            cacheFile.createNewFile()
        }
        return cacheFile
    }
}