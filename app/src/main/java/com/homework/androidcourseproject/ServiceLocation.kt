package com.homework.androidcourseproject

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import com.google.android.gms.maps.model.LatLng

//class ServiceLocation : Service() {
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        createNotification()
//        job()
//        return START_STICKY
//    }
//
//    private fun job() {
//        val runnable = Runnable {
//            markersMonitor
//        }
//        Thread(runnable).start()
//    }
//
//    private fun createNotification() {
//        val notification = Notification.Builder(applicationContext,"CHANNEL")
//            .setContentTitle("Point location monitoring").build()
//        startForeground(888, notification)
//    }
//
//    override fun onBind(intent: Intent?): Nothing? = null
//}