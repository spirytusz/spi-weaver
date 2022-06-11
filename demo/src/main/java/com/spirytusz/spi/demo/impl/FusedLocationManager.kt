package com.spirytusz.spi.demo.impl

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationManager
import com.spirytusz.spi.demo.api.ILocationService
import com.spirytusz.spi.weaver.runtime.annotation.ServiceImpl

@ServiceImpl(alias = "fused")
class FusedLocationManager : ILocationService {
    @SuppressLint("InlinedApi")
    override fun obtainSingleLocation(): Location {
        return Location(LocationManager.FUSED_PROVIDER)
    }
}