package com.spirytusz.spi.demo.impl

import android.location.Location
import android.location.LocationManager
import com.spirytusz.spi.demo.api.ILocationService
import com.spirytusz.spi.weaver.runtime.annotation.ServiceImpl

@ServiceImpl(alias = "coarse")
class CoarseLocationService : ILocationService {
    override fun obtainSingleLocation(): Location {
        return Location(LocationManager.NETWORK_PROVIDER)
    }
}