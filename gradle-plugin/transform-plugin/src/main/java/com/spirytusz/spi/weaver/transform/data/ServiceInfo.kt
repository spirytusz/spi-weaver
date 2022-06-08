package com.spirytusz.spi.weaver.transform.data

import com.google.gson.annotations.SerializedName

data class ServiceInfo(
    @SerializedName("service_name")
    val className: String = ""
)