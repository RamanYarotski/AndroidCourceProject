package com.homework.androidcourseproject

import android.location.Location
import android.location.LocationListener
import android.os.Bundle

class MyLocListener() : LocationListener {
    private lateinit var locListenerInterface: LocListenerInterface

    fun setLocListenerInterface ( locListenerInterface: LocListenerInterface){
        this.locListenerInterface = locListenerInterface
    }

    override fun onLocationChanged(location: Location) {
        locListenerInterface.OnLocationChanged(location)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        super.onStatusChanged(provider, status, extras)
    }

    override fun onProviderEnabled(provider: String) {
        super.onProviderEnabled(provider)
    }

    override fun onProviderDisabled(provider: String) {
        super.onProviderDisabled(provider)
    }

}