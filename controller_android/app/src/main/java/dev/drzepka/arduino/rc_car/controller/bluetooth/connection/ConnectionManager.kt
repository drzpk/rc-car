package dev.drzepka.arduino.rc_car.controller.bluetooth.connection

interface ConnectionManager {

    fun connect(mac: String)
    fun disconnect()
    fun sendData(data: ByteArray)

}