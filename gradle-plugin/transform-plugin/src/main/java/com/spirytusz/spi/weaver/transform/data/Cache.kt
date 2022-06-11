package com.spirytusz.spi.weaver.transform.data

import com.google.gson.annotations.SerializedName
import com.spirytusz.booster.annotation.Boost

@Boost
data class Cache(
    @SerializedName("cache_service_infos")
    val serviceInfoList: Set<ServiceInfo> = setOf(),
    @SerializedName("cache_service_impl_infos")
    val serviceImplInfoList: Set<ServiceImplInfo> = setOf()
)