package com.homework.androidcourseproject


import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.PersistableBundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity(), LocListenerInterface,
    GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener, OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback,
    GoogleMap.OnMarkerDragListener {

    private lateinit var line: PolylineOptions
    private var polylineFinal: Polyline? = null

    //GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
//com.google.android.gms.location.LocationListener{
    private lateinit var locationManager: LocationManager
    private var tvRestDistance: TextView? = null
    private var tvPassedDistance: TextView? = null
    private var lastLocation: Location? = null
    private lateinit var latLng: LatLng
    private lateinit var myLocListener: MyLocListener
    private var distance: Int = 0
    private var tvSpeed: TextView? = null
    private var pb: ProgressBar? = null
    private var passedDistance: Int = 0
    private var restDistance: Int = 0
    private val mSecToKmH: Float = 3.6F
    private lateinit var map: GoogleMap
    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var locationRequest: LocationRequest
    private val MINSK = LatLng(53.92126, 27.53078)
    private val ZOOM_LEVEL = 18f
    private var permissionDenied = false
    private lateinit var mapFragment: View
    private lateinit var setAndFindPositionButton: ImageButton
    private var isSetFindPositionButton: Boolean = false //Flag of check setAndFindPositionButton
    private var isSetVoicePositionButton: Boolean = false //Flag of check voicePositionButton
    private lateinit var cleanButton: ImageButton
    private lateinit var exitButton: ImageButton
    private lateinit var voicePositionButton: ImageButton
    private lateinit var walkingButton: ImageButton
    private lateinit var trafficButton: ImageButton
    private lateinit var userId: String
    private lateinit var marker: Marker
    private lateinit var markerLocation: Location
    private lateinit var markerOption: MarkerOptions
    private lateinit var voiceMarkerOption: MarkerOptions
    private var mAuth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null
    private var variablePointRef: DatabaseReference? = null
    private var voicePointsRef: DatabaseReference? = null
    private var voiceTextRef: DatabaseReference? = null
    private var geoFire: GeoFire? = null
    private var isQuest: Boolean = false

    private var voiceMarkers: ArrayList<Marker> = ArrayList()
    private var voiceMarkersLocation: ArrayList<Location> = ArrayList()
    private var voicePointNames: ArrayList<String> = ArrayList()
    private var voicePointTexts: ArrayList<String> = ArrayList()
    private var numberNotificationsPerDay: ArrayList<Int> = ArrayList()

    private var width by Delegates.notNull<Int>()
    private var isMarker: Boolean = false
    private var isVoiceMarker: Boolean = false
    private var numberDragMarker: Int? = null
    private lateinit var tts: TextToSpeech
    private var ttsEnabled: Boolean = false
    var updateCounters: String = "update counters"
    private var voiceMarkersReady: Boolean = false
    private lateinit var lastVoiceMarker: Marker


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()

        walkingButton.setOnClickListener {
            showDialog()
        }

        exitButton.setOnClickListener {
            if (!isQuest) {
                mAuth?.signOut()
//                logOutUser()
            }
            exitToSignInActivity()
        }

        cleanButton.setOnClickListener {
            if (::marker.isInitialized) {
                marker.remove()
            }
            polylineFinal?.remove()
            turnOFFSetAndFindPositionButton()
            distance = 0
            passedDistance = 0
            restDistance = 0
            tvRestDistance?.text = restDistance.toString()
            tvPassedDistance?.text = passedDistance.toString()
        }

        trafficButton.setOnClickListener {
            map.isTrafficEnabled = !map.isTrafficEnabled
        }

        setAndFindPositionButton.setOnClickListener {
            if (!::latLng.isInitialized) {
                loadingMessage()
            } else {
                if (!isSetFindPositionButton) {   //Set marker and write current location to DB
                    turnONNSetAndFindPositionButton()
                    if (::marker.isInitialized) {
                        marker.remove()
                    }
                    polylineFinal?.remove()
                    addCurrentMarker(latLng, "My point")
                    moveAndZoomCamera(latLng)
                    if (!isQuest) {
                        writeVariablePointToDB(lastLocation!!, variablePointRef)
                    }
                } else if (isSetFindPositionButton) { //building a route
                    turnOFFSetAndFindPositionButton()
                    buildingRoute()
                }
            }
        }

        voicePositionButton.setOnClickListener {
            if (!::latLng.isInitialized) {
                loadingMessage()
            } else {
                if (!isSetVoicePositionButton) {
                    setTextModeOfVoicePositionButton()
                    enterVoicePointDialog()
                } else if (isSetVoicePositionButton) {
                    addMarkerModeOfVoicePositionButton()
                    addVoiceMarker(
                        latLng,
                        voicePointNames[voicePointNames.size - 1],
                        voicePointTexts[voicePointTexts.size - 1],
                        lastLocation
                    )
                    voiceMarkersReady = false
                    if (!isQuest) {
                        writeVariablePointToDB(lastLocation!!, voicePointsRef)
//                Передача текста в firebase
//                voiceTextRef = FirebaseDatabase.getInstance().reference.child("Voice text")
                    }
                }
            }
        }

        startFirstUpdateWork()
//        startService(Intent(this@MainActivity, ServiceLocation::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
//        if (voiceMarkers.isEmpty()) {
//            stopService(Intent(this@MainActivity, ServiceLocation::class.java))
//        }
    }

    private fun startFirstUpdateWork() {
        val constraints = Constraints.Builder().build()
//        val work = PeriodicWorkRequest.Builder(DailyWorker::class.java,
//            0, TimeUnit.HOURS,
//            1, TimeUnit.MINUTES)
//            .setConstraints(constraints).build()
//        val workManager = WorkManager.getInstance(this)
//        workManager.enqueueUniquePeriodicWork(updateCounters, ExistingPeriodicWorkPolicy.KEEP,work)

        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
// Set Execution around 05:00:00 AM
        dueDate.set(Calendar.HOUR_OF_DAY, 5)
        dueDate.set(Calendar.MINUTE, 30)
        dueDate.set(Calendar.SECOND, 0)
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
            updateCounterNotification()
        }
        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
        val dailyWorkRequest = OneTimeWorkRequest.Builder(DailyWorker::class.java)
            .setConstraints(constraints).setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .addTag(updateCounters).build()
        WorkManager.getInstance(this).enqueue(dailyWorkRequest)
    }

    private fun markersMonitor() {
        if (!voiceMarkersLocation.size.equals(null) && ::latLng.isInitialized) {
            for (i in 0 until voiceMarkersLocation.size) {
                if (lastLocation?.distanceTo(voiceMarkersLocation[i])?.toInt()!! <= 10) {
                    if (voiceMarkersReady && numberNotificationsPerDay[i] > 0
                        && voiceMarkers[i] != lastVoiceMarker) {
                        numberNotificationsPerDay[i] = numberNotificationsPerDay[i] - 1
//                        Notification.Builder(this.getApplicationContext())
//                            .setSound(speak(voicePointTexts[i]))
                        speak(voicePointTexts[i])
                        lastVoiceMarker = voiceMarkers[i]
                    }
                }
            }

        }

//        voicePointsRef = FirebaseDatabase.getInstance().reference
//            .child("Users").child(userId).child("Voice points")
//        geoFire = GeoFire(voicePointsRef)

    }

    fun updateCounterNotification() {
        Collections.replaceAll(numberNotificationsPerDay, 0, 2)
    }

    private fun playVoicePointNotification(voicePointNumber: Int) {

    }


    private fun readPointFromDB(userPointsRef: DatabaseReference?): LatLng {
        val lat = userPointsRef?.child(userId)?.child("l")?.child("0")
            ?.get().toString().toDouble()
        val lng = userPointsRef?.child(userId)?.child("l")?.child("1")
            ?.get().toString().toDouble()
        return LatLng(lat, lng)
    }

    private fun init() {
        val loginIntent = intent
        isQuest = loginIntent.getBooleanExtra("isQuest", false)
        tvRestDistance = findViewById(R.id.rest_distanceTV)
        tvPassedDistance = findViewById(R.id.passed_distanceTV)
        tvSpeed = findViewById(R.id.speedTV)
        pb = findViewById(R.id.progressBar)
        mapFragment = findViewById(R.id.map)
        pb?.max = 1000
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        myLocListener = MyLocListener()
        myLocListener.setLocListenerInterface(this)
        checkPermissions()
        val mapFragment: SupportMapFragment? = supportFragmentManager
            .findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        setAndFindPositionButton = findViewById(R.id.setAndFindPositionButton)
        cleanButton = findViewById(R.id.cleanButton)
        exitButton = findViewById(R.id.exitButton)
        voicePositionButton = findViewById(R.id.voicePositionButton)
        walkingButton = findViewById(R.id.walkingButton)
        trafficButton = findViewById(R.id.trafficButton)
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth?.currentUser
        userId = intent.getStringExtra("userID").toString()
        variablePointRef = FirebaseDatabase.getInstance().reference
            .child("Users").child(userId).child("Variable point")
        voicePointsRef = FirebaseDatabase.getInstance().reference
            .child("Users").child(userId).child("Voice points")

        width = resources.displayMetrics.widthPixels

        tts = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                if (tts.isLanguageAvailable(Locale(Locale.getDefault().language))
                    == TextToSpeech.LANG_AVAILABLE
                ) {
                    tts.language = Locale(Locale.getDefault().language)
                } else {
                    tts.language = Locale.US
                }
                tts.setPitch(1f)
                tts.setSpeechRate(1f)
                ttsEnabled = true
            } else if (it == TextToSpeech.ERROR) {
                Toast.makeText(this, R.string.tts_error, Toast.LENGTH_LONG).show()
                ttsEnabled = false
            }
        })
    }


    private fun speak(text: String) {
        if (!ttsEnabled) return
        val utteranceId = this.hashCode().toString() + ""
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }


    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap ?: return
        googleMap.setOnMyLocationButtonClickListener(this)
        googleMap.setOnMyLocationClickListener(this)
        googleMap.setOnMarkerDragListener(this)
        enableMyLocation()
        map.uiSettings.isMapToolbarEnabled = true
        map.uiSettings.isZoomGesturesEnabled = true

        map.setOnInfoWindowLongClickListener {
            for (i in 0 until voiceMarkers.size) {
                if (voiceMarkers[i] == it) {
                    showDeleteDialog(i)
                    break
                }
            }
        }
    }

    private fun getRoute() {
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(
        outState: Bundle,
        outPersistentState: PersistableBundle
    ) {
        super.onSaveInstanceState(outState, outPersistentState)
    }

    private fun addCurrentMarker(latLon: LatLng, title: String) {
        markerOption = MarkerOptions().position(latLon).draggable(true)
            .title(title).icon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            )
        marker = map.addMarker(markerOption)
    }

    private fun addVoiceMarker(
        latLon: LatLng,
        title: String?,
        snippet: String?,
        location: Location?
    ) {
        voiceMarkerOption = MarkerOptions().position(latLon).draggable(true)
            .title(title).snippet(snippet).icon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            )
        voiceMarkers.add(map.addMarker(voiceMarkerOption))
        if (!::lastVoiceMarker.isInitialized) {
            val firstVoiceMarkerOption =
                MarkerOptions().position(MINSK).alpha(0.0F).draggable(true).icon(
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            lastVoiceMarker = map.addMarker(firstVoiceMarkerOption)
        }
        voiceMarkersLocation.add(location!!)
    }

    //    * Enables the My Location layer if the fine location permission has been granted.
    private fun enableMyLocation() {
        if (!::map.isInitialized) return
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        } else {
            checkPermissions()
        }
    }

    private fun setDistance(distance: String) {
        pb?.max = distance.toInt()
        restDistance = distance.toInt()
        this.distance = distance.toInt()
        tvRestDistance?.text = distance
    }

    private fun enterVoicePointDialog() {
        val builder2 = AlertDialog.Builder(this)
        builder2.setTitle(R.string.dialog_title_voice)
        val cl2: ConstraintLayout =
            layoutInflater.inflate(
                R.layout.dialog_layout_voice_point,
                null
            ) as ConstraintLayout
        builder2.setPositiveButton(R.string.dialog_button_ok) { dialog2, _ ->
            val ad2: AlertDialog = dialog2 as AlertDialog
            val edName: EditText? = ad2.findViewById(R.id.edNameVoicePoint)
            val edText: EditText? = ad2.findViewById(R.id.edTextVoicePoint)
//            val edNumber: EditText? = ad2.findViewById(R.id.edNumberNotifications)
            writeVoicePointInfo(
                edName?.text.toString(),
                edText?.text.toString()
            )
        }
        builder2.setView(cl2).show()
    }

    private fun writeVoicePointInfo(name: String, text: String) {
        voicePointNames.add(name)
        voicePointTexts.add(text)
        numberNotificationsPerDay.add(2)
    }

    private fun showDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.dialog_title)
        val cl: ConstraintLayout =
            layoutInflater.inflate(
                R.layout.dialog_layout_current_point,
                null
            ) as ConstraintLayout
        builder.setPositiveButton(
            R.string.dialog_button_ok,
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

    private fun showDeleteDialog(numberOfMarker: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.dialog_delete_marker)
        val cl: ConstraintLayout =
            layoutInflater.inflate(R.layout.dialog_delete_marker, null) as ConstraintLayout
        builder.setPositiveButton(
            R.string.dialog_button_ok,
            DialogInterface.OnClickListener { dialog, which ->
                val ad: AlertDialog = dialog as AlertDialog
                voiceMarkers[numberOfMarker].remove()
                voiceMarkers.removeAt(numberOfMarker)
                voiceMarkersLocation.removeAt(numberOfMarker)
                voicePointNames.removeAt(numberOfMarker)
                voicePointTexts.removeAt(numberOfMarker)
                numberNotificationsPerDay.removeAt(numberOfMarker)
            })
        builder.setNegativeButton(
            R.string.dialog_button_cancel,
            DialogInterface.OnClickListener { dialog, which ->
                val ad: AlertDialog = dialog as AlertDialog
            })
        builder.setView(cl).show()
    }

    private fun updateDistance(location: Location) {
        if (location.hasSpeed() && location.speed > 0.5) {
            if (distance > passedDistance) {
                passedDistance += lastLocation?.distanceTo(location)?.toInt() ?: 0
            }
            if (restDistance > 0) {
                moveAndZoomCamera(latLng)
                restDistance -= lastLocation?.distanceTo(location)?.toInt() ?: 0
                pb?.progress = passedDistance
            }
            if (restDistance <= 0) {
                distance = 0
                passedDistance = 0
                restDistance = 0
            }
            tvRestDistance?.text = restDistance.toString()
            tvPassedDistance?.text = passedDistance.toString()
            tvSpeed?.text = (location.speed * mSecToKmH).toInt().toString()
        }

    }

    private fun moveAndZoomCamera(latLng: LatLng) {
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        map.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults[0] == RESULT_OK) {
            checkPermissions()
            enableMyLocation()
        } else {
            // Permission was denied. Display an error message
            permissionDenied = true
        }
    }

    // [END maps_check_location_permission_result]
    override fun onResumeFragments() {
        super.onResumeFragments()
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            Snackbar.make(
                mapFragment, "Permission was not granted",
                Snackbar.LENGTH_LONG
            ).setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.black
                )
            ).show()
            permissionDenied = false
        }
    }


    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 2000, 1F, myLocListener
            )
        }
    }

    override fun onLocationChanged(location: Location) {
        latLng = LatLng(location.latitude, location.longitude)
        updateDistance(location)
        lastLocation = location
        markersMonitor()
    }

    private fun turnOFFSetAndFindPositionButton() {
        isSetFindPositionButton = false
        setAndFindPositionButton.background = ContextCompat.getDrawable(
            this, R.drawable.ic_twotone_add_location_48
        )
    }

    private fun turnONNSetAndFindPositionButton() {
        isSetFindPositionButton = true
        setAndFindPositionButton.background = ContextCompat.getDrawable(
            this, R.drawable.ic_baseline_not_find_route_location_48
        )
    }

    private fun addMarkerModeOfVoicePositionButton() {
        isSetVoicePositionButton = false
        voicePositionButton.background = ContextCompat.getDrawable(
            this, R.drawable.ic_twotone_edit_location_alt_48
        )
    }

    private fun setTextModeOfVoicePositionButton() {
        isSetVoicePositionButton = true
        voicePositionButton.background = ContextCompat.getDrawable(
            this, R.drawable.ic_twotone_add_location_alt_48
        )
    }

    private fun buildingRoute() {
        val geoApiContext = GeoApiContext.Builder()
            .apiKey(this.resources.getString(R.string.google_maps_key))
            .build()

        var result: DirectionsResult? = null

        val markerPosition: LatLng = if (!isQuest) {
            readPointFromDB(variablePointRef)
        } else LatLng(marker.position.latitude, marker.position.longitude)

        try {
            result = DirectionsApi.newRequest(geoApiContext)
                .mode(TravelMode.WALKING)
                .origin(com.google.maps.model.LatLng(latLng.latitude, latLng.longitude))
                .destination(
                    com.google.maps.model.LatLng(
                        markerPosition.latitude,
                        markerPosition.longitude
                    )
                )
                .await()
        } catch (e: ApiException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val path = result?.routes?.get(0)?.overviewPolyline?.decodePath()
        line = PolylineOptions().width(16F).color(R.color.line_color)
        val latLngBuilder = LatLngBounds.Builder()
        for (i in path!!.indices) {
            line.add(LatLng(path[i].lat, path[i].lng))
            latLngBuilder.include(LatLng(path[i].lat, path[i].lng))
        }
        polylineFinal = map.addPolyline(line)
        val latLngBounds = latLngBuilder.build()
        val track = CameraUpdateFactory.newLatLngBounds(latLngBounds, width, width, 25)

        map.moveCamera(track)
    }

    private fun writeVariablePointToDB(location: Location, userPointsRef: DatabaseReference?) {
        geoFire = GeoFire(userPointsRef)
        geoFire?.setLocation(userId, GeoLocation(location.latitude, location.longitude))
    }

    private fun writeVoicePointToDB(location: Location, userPointsRef: DatabaseReference?) {
        geoFire = GeoFire(userPointsRef)
        geoFire?.setLocation(userId, GeoLocation(location.latitude, location.longitude))
//        userPointsRef.knjb
    }

//    private fun logOutUser() {
//        geoFire = GeoFire(userPointsRef)
//        geoFire?.removeLocation(userId)
//    }

    override fun onStop() {
        super.onStop()
    }

    private fun exitToSignInActivity() {
        val outIntent = Intent(this, SignInActivity::class.java)
        startActivity(outIntent)
        finish()
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "MyLocation", Toast.LENGTH_SHORT).show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(this, "Current location:\n$location", Toast.LENGTH_LONG).show()
    }


//    override fun onConnected(p0: Bundle?) {
//        locationRequest = LocationRequest()
//        locationRequest.apply {
//            interval = 1000
//            fastestInterval=1000
//            priority = MAX_PRIORITY
//            }
//            if (ActivityCompat.checkSelfPermission(
//                    this, Manifest.permission.ACCESS_FINE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(
//                    this, Manifest.permission.ACCESS_COARSE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                return
//            }
//            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,
//            locationRequest,this@MainActivity)
//    }

    //    override fun onConnectionSuspended(p0: Int) {
//        TODO("Not yet implemented")
//    }
//
//    override fun onConnectionFailed(p0: ConnectionResult) {
//        TODO("Not yet implemented")
//    }
//
//    override fun onLocationChanged(p0: Location?) {
//        TODO("Not yet implemented")
//    }
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }

    override fun onMarkerDragStart(p0: Marker?) {
        if (::marker.isInitialized) {
            if (p0?.position == marker.position) {
                isMarker = true
            }
        } else {
            for (i in 0 until voiceMarkers.size) {
                if (p0?.position == voiceMarkers[i].position) {
                    isVoiceMarker = true
                    numberDragMarker = i
                }
            }
        }
    }

    override fun onMarkerDrag(p0: Marker?) {
    }

    override fun onMarkerDragEnd(p0: Marker?) {
        if (isMarker) {
            marker.position = p0?.position
            isMarker = false
        } else if (isVoiceMarker && p0 != null) {
            voiceMarkers[numberDragMarker!!] = p0
            val loc = Location("")
            loc.latitude = p0.position.latitude
            loc.longitude = p0.position.longitude
            voiceMarkersLocation[numberDragMarker!!] = loc
            isVoiceMarker = false
            numberDragMarker = null
            voiceMarkersReady = true
        }
    }

    private fun loadingMessage() {
        Toast.makeText(
            this, "Loading location...Wait, please, and try again",
            Toast.LENGTH_LONG
        ).show()
    }

    inner class DailyWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

        override fun doWork(): Result {
            this@MainActivity.updateCounterNotification()
            val currentDate = android.icu.util.Calendar.getInstance()
            val dueDate = android.icu.util.Calendar.getInstance()
            // Set Execution around 05:00:00 AM
            dueDate.set(android.icu.util.Calendar.HOUR_OF_DAY, 5)
            dueDate.set(android.icu.util.Calendar.MINUTE, 30)
            dueDate.set(android.icu.util.Calendar.SECOND, 0)
            if (dueDate.before(currentDate)) {
                dueDate.add(android.icu.util.Calendar.HOUR_OF_DAY, 24)
            }
            val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
            val dailyWorkRequest = OneTimeWorkRequest.Builder(DailyWorker::class.java)
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .addTag(updateCounters)
                .build()
            WorkManager.getInstance(applicationContext)
                .enqueue(dailyWorkRequest)
            return Result.success()
        }
    }
}

