package dev.drzepka.arduino.rc_car.controller.bluetooth.connection

interface ConnectionManager {
    val isConnected: Boolean
    var listener: Listener?

    fun connect(mac: String)
    fun disconnect()
    fun sendData(data: ByteArray)

    interface Listener {
        fun onConnectionLost(message: String)
    }
}