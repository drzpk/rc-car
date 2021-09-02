package dev.drzepka.arduino.rc_car.controller.bluetooth.connection

import android.app.Application
import android.bluetooth.BluetoothManager
import java.util.*

class RealConnectionManager(application: Application) : ConnectionManager {

    private val manager = application.getSystemService(BluetoothManager::class.java)

    override fun connect(mac: String) {
        val device = manager.adapter.getRemoteDevice(mac)
        val socket = device.createRfcommSocketToServiceRecord(UUID.randomUUID())
        socket.connect()
    }

    override fun disconnect() {

    }

    override fun sendData(data: ByteArray) {

    }
}