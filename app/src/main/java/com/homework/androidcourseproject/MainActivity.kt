package com.homework.androidcourseproject

import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity(), LocListenerInterface {
    private lateinit var locationManager: LocationManager
    private var tvRestDistance: TextView? = null
    private var tvPassedDistance: TextView? = null
    private var lastLocation: Location? = null
    private lateinit var myLocListener: MyLocListener
    private var distance: Int = 0
    private var tvSpeed: TextView? = null
    private var pb: ProgressBar? = null
    private var passedDistance: Int = 0
    private var restDistance: Int = 0
    private val mSecToKmH: Float = 3.6F


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        tvRestDistance?.setOnClickListener {
            showDialog()
        }
    }

    private fun init() {
        tvRestDistance = findViewById(R.id.rest_distanceTV)
        tvPassedDistance = findViewById(R.id.passed_distanceTV)
        tvSpeed = findViewById(R.id.speedTV)
        pb = findViewById(R.id.progressBar)
        pb?.max = 1000
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        myLocListener = MyLocListener()
        myLocListener.setLocListenerInterface(this)
        checkPermissions()
    }

    private fun setDistance(distance: String) {
        pb?.max = distance.toInt()
        restDistance = distance.toInt()
        this.distance = distance.toInt()
        tvRestDistance?.text = distance
    }

    private fun showDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.dialog_title)
        val cl: ConstraintLayout =
            layoutInflater.inflate(R.layout.dialog_layout, null) as ConstraintLayout

        builder.setPositiveButton(
            R.string.dialog_button,
            DialogInterface.OnClickListener { dialog, which ->
                val ad: AlertDialog = dialog as AlertDialog
                val ed: EditText? = ad.findViewById(R.id.edText)
                var tx: String = ed?.text.toString()
                if (tx != "") {
                    setDistance(tx)
                } else {
                    tx = "0"
                }
            })
        builder.setView(cl).show()
    }

    private fun updateDistance(location: Location) {
        if (location.hasSpeed() && location.speed > 0.5) {
            if (distance > passedDistance) passedDistance += lastLocation?.distanceTo(location)
                ?.toInt() ?: 0
            if (restDistance > 0) restDistance -= lastLocation?.distanceTo(location)?.toInt() ?: 0
            pb?.progress = passedDistance
        }
        lastLocation = location
        tvRestDistance?.text = restDistance.toString()
        tvPassedDistance?.text = passedDistance.toString()
        tvSpeed?.text = (location.speed * mSecToKmH).toString()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults[0] == RESULT_OK) {
            checkPermissions()
        }
    }

    private fun checkPermissions() {
        if (
            (ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
                    != PackageManager.PERMISSION_GRANTED)
            && (ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            )
                    != PackageManager.PERMISSION_GRANTED)
        ) {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ), 100
            )
        } else {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 2000, 1F, myLocListener
            )
        }
    }

    override fun OnLocationChanged(location: Location) {
        updateDistance(location)
    }
}