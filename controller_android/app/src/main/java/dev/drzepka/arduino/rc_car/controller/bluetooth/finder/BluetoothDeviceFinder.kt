package dev.drzepka.arduino.rc_car.controller.bluetooth.finder

import dev.drzepka.arduino.rc_car.controller.model.BluetoothDeviceData

abstract class BluetoothDeviceFinder {

    var listener: BluetoothDeviceListener? = null

    abstract fun start(): Boolean
    abstract fun stop()

    interface BluetoothDeviceListener {
        fun onSearchStarted()
        fun onDeviceFound(deviceData: BluetoothDeviceData)
        fun onSearchCompleted()
    }
}