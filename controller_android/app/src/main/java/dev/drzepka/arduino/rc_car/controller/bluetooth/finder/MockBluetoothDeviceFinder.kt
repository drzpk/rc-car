package dev.drzepka.arduino.rc_car.controller.bluetooth.finder

import android.os.Handler
import android.os.Looper
import dev.drzepka.arduino.rc_car.controller.model.BluetoothDeviceData
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.random.Random

class MockBluetoothDeviceFinder : BluetoothDeviceFinder() {

    private val working = AtomicBoolean(false)

    override fun start(): Boolean {
        if (working.get())
            return false

        val willStart = Random.nextInt(4) > 0
        if (!willStart)
            return false

        val foundDevices = Random.nextInt(2, RANDOM_BLUETOOTH_DEVICES.size)
        val shuffled = RANDOM_BLUETOOTH_DEVICES.shuffled().subList(0, foundDevices)

        working.set(true)
        listener?.apply { this.onSearchStarted() }

        thread {
            var counter = 0
            while (counter < foundDevices) {
                sleep(Random.nextLong(100, 3000))

                if (!working.get())
                    break

                val finalCounter = counter
                Handler(Looper.getMainLooper()).post {
                    listener?.apply { this.onDeviceFound(shuffled[finalCounter]) }
                }

                counter++
            }

            Handler(Looper.getMainLooper()).post {
                listener?.apply { this.onSearchCompleted() }
            }
        }

        return true
    }

    override fun stop() {
        working.set(false)
    }

    companion object {
        private val RANDOM_BLUETOOTH_DEVICES = listOf(
            BluetoothDeviceData("Simple device 1", "aa:bb:cc"),
            BluetoothDeviceData("Simple device 2", "aa:bb:cc"),
            BluetoothDeviceData("Simple device 3", "aa:bb:cc"),
            BluetoothDeviceData("Simple device 4", "aa:bb:cc"),
            BluetoothDeviceData("Simple device 5", "aa:bb:cc"),
            BluetoothDeviceData("Simple device 6", "aa:bb:cc"),
            BluetoothDeviceData("Simple device 7", "aa:bb:cc")
        )
    }

}