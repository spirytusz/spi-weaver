package com.spirytusz.spi.weaver.transform

import com.android.build.gradle.AppExtension
import com.spirytusz.spi.weaver.config.ConfigProvider
import com.spirytusz.spi.weaver.transform.global.TransformContext
import org.gradle.api.Plugin
import org.gradle.api.Project

class ServiceProviderWeaverPlugin : Plugin<Project> {

    companion object {
        private const val TAG = "ServiceProviderWeaverPlugin"
    }

    override fun apply(target: Project) {
        val appExtension = target.extensions.getByType(AppExtension::class.java)
        val context = TransformContext(target, ConfigProvider(target))
        appExtension.registerTransform(ServiceProviderTransform(context))
    }
}