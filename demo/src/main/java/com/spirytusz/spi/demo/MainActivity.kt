package com.spirytusz.spi.demo

import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.spirytusz.spi.demo.api.ILocationService
import com.spirytusz.spi.demo.databinding.ActivityMainBinding
import com.spirytusz.spi.weaver.runtime.ServiceProvider

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        fetchLocation()
    }

    private fun fetchLocation() {
        val networkLocation = ServiceProvider.of("coarse", ILocationService::class.java)
            ?.obtainSingleLocation()
        val gpsLocation = ServiceProvider.of("fine", ILocationService::class.java)
            ?.obtainSingleLocation()
        val fusedLocation = ServiceProvider.of("fused", ILocationService::class.java)
            ?.obtainSingleLocation()
        binding.location = buildString {
            append("coarse: ${networkLocation.simpleInfo()}\n")
            append("fine: ${gpsLocation.simpleInfo()}\n")
            append("fused: ${fusedLocation.simpleInfo()}")
        }
    }

    private fun Location?.simpleInfo(): String {
        return this?.let { "[$provider] lat=$latitude, lon=$longitude" } ?: ""
    }
}