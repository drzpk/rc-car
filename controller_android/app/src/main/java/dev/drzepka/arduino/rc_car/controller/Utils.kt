package dev.drzepka.arduino.rc_car.controller

import android.os.Build

object Utils {

    fun isEmulator(): Boolean = Build.FINGERPRINT.contains("generic")
}