package dev.drzepka.arduino.rc_car.controller.bluetooth.finder

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import dev.drzepka.arduino.rc_car.controller.model.BluetoothDeviceData

class RealBluetoothDeviceFinder(private val application: Application) : BluetoothDeviceFinder() {

    private val bluetoothManager = application.getSystemService(BluetoothManager::class.java)

    private var started = false
    private var receiver: BroadcastReceiver? = null

    override fun start() {
        if (started)
            return
        started = true

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                        val data = BluetoothDeviceData(device.name, device.address)
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

        bluetoothManager.adapter.startDiscovery()
    }

    override fun stop() {
        if (!started)
            return
        started = false

        application.unregisterReceiver(receiver!!)
        bluetoothManager.adapter.cancelDiscovery()
    }
}