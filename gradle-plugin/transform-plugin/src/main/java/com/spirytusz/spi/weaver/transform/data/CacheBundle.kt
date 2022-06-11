package com.spirytusz.spi.weaver.transform.data

import com.google.gson.annotations.SerializedName
import com.spirytusz.booster.annotation.Boost

@Boost
data class CacheBundle(
    @SerializedName("bundle")
    val bundle: Map<String, Cache> = mapOf()
)