package com.homework.androidcourseproject

import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity(), LocListenerInterface {
    private lateinit var locationManager: LocationManager
    private var tvDistance: TextView? = null
    private var lastLocation: Location? = null
    private lateinit var myLocListener:MyLocListener
    private var distance: Int = 0
    private var tvSpeed: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        tvDistance = findViewById(R.id.distanceTextView)
        tvSpeed = findViewById(R.id.speedTextView)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        myLocListener = MyLocListener()
        myLocListener.setLocListenerInterface(this)
        checkPermissions()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == 100 && grantResults[0] == RESULT_OK){
        checkPermissions()
    }
    }

    private fun checkPermissions() {
        if (
                (ActivityCompat.checkSelfPermission(
                        this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)
                && (ActivityCompat.checkSelfPermission(
                        this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)
        )
                        {
                            requestPermissions(arrayOf(
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION), 100)
        } else
        {
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,2,1F,myLocListener)
        }
    }

    override fun OnLocationChanged(location: Location) {
    if (location.hasSpeed()){
        distance += lastLocation?.distanceTo(location)?.toInt() ?: 0
    }
        lastLocation = location
        tvDistance?.text = distance.toString()
        tvSpeed?.text =location.speed.toString()
    }
}