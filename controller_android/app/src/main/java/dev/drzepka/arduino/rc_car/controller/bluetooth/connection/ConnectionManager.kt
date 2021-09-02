package dev.drzepka.arduino.rc_car.controller.bluetooth.connection

interface ConnectionManager {
    val isConnected: Boolean

    fun connect(mac: String)
    fun disconnect()
    fun sendData(data: ByteArray)

}