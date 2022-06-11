package com.spirytusz.spi.weaver.transform.global

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.spirytusz.adapter.GeneratedTypeAdapterFactory
import com.spirytusz.spi.weaver.config.ConfigProvider
import org.gradle.api.Project

class TransformContext(val project: Project, val configProvider: ConfigProvider) {

    val gson: Gson = GsonBuilder()
        .registerTypeAdapterFactory(GeneratedTypeAdapterFactory())
        .create()
}