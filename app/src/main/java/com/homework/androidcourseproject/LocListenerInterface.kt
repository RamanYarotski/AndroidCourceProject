package com.homework.androidcourseproject

import android.location.Location

interface LocListenerInterface {
    fun OnLocationChanged (location: Location)
}