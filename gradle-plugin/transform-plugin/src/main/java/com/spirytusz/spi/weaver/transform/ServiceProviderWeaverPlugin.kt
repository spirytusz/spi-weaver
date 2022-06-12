package com.spirytusz.spi.weaver.transform

import com.android.build.gradle.AppExtension
import com.spirytusz.spi.weaver.config.ConfigProvider
import com.spirytusz.spi.weaver.log.Logger
import com.spirytusz.spi.weaver.transform.global.TransformContext
import org.gradle.api.Plugin
import org.gradle.api.Project

class ServiceProviderWeaverPlugin : Plugin<Project> {

    companion object {
        private const val TAG = "ServiceProviderWeaverPlugin"
    }

    override fun apply(target: Project) {
        if (target.extensions.findByType(AppExtension::class.java) == null) {
            Logger.i(TAG) {
                "Plugin [com.spirytusz.spi.weaver] can not be applied on non-android project"
            }
            return
        }
        val appExtension = target.extensions.getByType(AppExtension::class.java)
        val context = TransformContext(target, ConfigProvider(target))
        appExtension.registerTransform(ServiceProviderTransform(context))
    }
}