package com.spirytusz.spi.weaver.transform.data

import com.google.gson.annotations.SerializedName
import com.spirytusz.booster.annotation.Boost

@Boost
data class ServiceImplInfo(
    @SerializedName("service_impl_name")
    val className: String = "",
    @SerializedName("service_impl_alias")
    val alias: String = "",
    @SerializedName("service_impl_implements")
    val implements: List<String> = listOf()
)