package dev.drzepka.arduino.rc_car.controller.bluetooth.connection

import android.app.Application
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.util.*

class RealConnectionManager(application: Application) : ConnectionManager {
    override val isConnected: Boolean
        get() = socket?.isConnected == true
    override var listener: ConnectionManager.Listener? = null

    private val manager = application.getSystemService(BluetoothManager::class.java)
    private var socket: BluetoothSocket? = null

    override fun connect(mac: String) {
        Log.i(TAG, "Connecting to $mac")
        val device = manager.adapter.getRemoteDevice(mac)
        socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
        socket?.connect()
    }

    override fun disconnect() {
        try {
            if (isConnected)
                socket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error while disconnecting the socket", e)
        }

        socket = null
    }

    override fun sendData(data: ByteArray) {
        try {
            socket?.outputStream?.write(data)
        } catch (e: Exception) {
            Log.e(TAG, "Error while sending data", e)
            listener?.onConnectionLost(e.message ?: "")
            disconnect()
        }
    }

    companion object {
        private const val TAG = "RealConnectionManager"
    }
}