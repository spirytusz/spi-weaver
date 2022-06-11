package com.spirytusz.spi.demo.api

import android.location.Location
import com.spirytusz.spi.weaver.runtime.annotation.Service

@Service
interface ILocationService {

    fun obtainSingleLocation(): Location
}