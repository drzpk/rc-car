package dev.drzepka.arduino.rc_car.controller.bluetooth.connection

import android.util.Log
import kotlin.random.Random

class MockConnectionManager : ConnectionManager {

    override fun connect(mac: String) {
        Thread.sleep(Random.nextLong(400, 1500))
        val success = Random.nextInt(4) > 0
        if (!success)
            throw Exception("Mock connection error")
    }

    override fun disconnect() {
        // nothing
    }

    override fun sendData(data: ByteArray) {
        Log.d("rc_car_ctrl", "Sending data")
    }
}