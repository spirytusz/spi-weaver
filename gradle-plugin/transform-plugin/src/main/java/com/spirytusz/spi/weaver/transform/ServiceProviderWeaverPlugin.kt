package com.spirytusz.spi.weaver.transform

import com.android.build.gradle.AppExtension
import com.spirytusz.spi.weaver.config.ConfigProvider
import org.gradle.api.Plugin
import org.gradle.api.Project

class ServiceProviderWeaverPlugin : Plugin<Project> {

    companion object {
        private const val TAG = "ServiceProviderWeaverPlugin"
    }

    override fun apply(target: Project) {
        val appExtension = target.extensions.getByType(AppExtension::class.java)
        appExtension.registerTransform(ServiceProviderTransform(target, ConfigProvider(target)))
    }
}