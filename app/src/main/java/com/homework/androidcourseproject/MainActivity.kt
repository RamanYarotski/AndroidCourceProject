package com.homework.androidcourseproject

import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity(), LocListenerInterface {
    private lateinit var locationManager: LocationManager
    private lateinit var distance: TextView
    private lateinit var lastLocation: Location
    private lateinit var myLocListener:MyLocListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        myLocListener = MyLocListener()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == 100 && grantResults[0] == RESULT_OK){
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2, 1,)
    }
        else{
            Toast.makeText(this, "Нет разрешения на GPS", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
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
        } else {

        }
    }

    override fun OnLocationChanged(location: Location) {
    }
}