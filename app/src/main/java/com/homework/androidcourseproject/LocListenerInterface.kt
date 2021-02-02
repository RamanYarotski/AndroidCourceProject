package com.homework.androidcourseproject

import android.location.Location

interface LocListenerInterface {
    fun onLocationChanged (location: Location)
}