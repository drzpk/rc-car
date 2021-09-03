package dev.drzepka.arduino.rc_car.controller.bluetooth.finder

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.util.Log
import dev.drzepka.arduino.rc_car.controller.model.BluetoothDeviceData

class RealBluetoothDeviceFinder(private val application: Application) : BluetoothDeviceFinder() {

    private val bluetoothManager = application.getSystemService(BluetoothManager::class.java)

    private var started = false
    private var receiver: BroadcastReceiver? = null

    override fun start(): Boolean {
        if (started)
            return false

        Log.i(TAG, "Starting searching for devices")
        if (!isLocationEnabled()) {
            Log.i(TAG, "Location is not enabled")
            return false
        }

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d(TAG, "Received action: " + intent.action)

                when (intent.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                        val name = device.name ?: "<no name>"
                        val data = BluetoothDeviceData(name, device.address)
                        listener?.apply { this.onDeviceFound(data) }
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                        listener?.apply { this.onSearchStarted() }
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        listener?.apply { this.onSearchCompleted() }
                        stop()
                    }
                }
            }
        }

        application.registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        application.registerReceiver(receiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED))
        application.registerReceiver(receiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))

        val status = bluetoothManager.adapter.startDiscovery()
        Log.i(TAG, "startDiscovery() status: $status (bluetooth state: ${bluetoothManager.adapter.state})")
        started = status

        return status
    }

    override fun stop() {
        if (!started)
            return

        Log.i(TAG, "Stopping searching for devices")
        started = false

        application.unregisterReceiver(receiver!!)
        bluetoothManager.adapter.cancelDiscovery()
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = application.getSystemService(LocationManager::class.java)
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    companion object {
        private const val TAG = "DeviceFinder"
    }
}