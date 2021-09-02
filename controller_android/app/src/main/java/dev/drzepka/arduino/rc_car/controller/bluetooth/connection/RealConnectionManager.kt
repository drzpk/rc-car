package dev.drzepka.arduino.rc_car.controller.bluetooth.connection

import android.app.Application
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import java.util.*

class RealConnectionManager(application: Application) : ConnectionManager {
    override val isConnected: Boolean
        get() = socket?.isConnected == true

    private val manager = application.getSystemService(BluetoothManager::class.java)
    private var socket: BluetoothSocket? = null

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