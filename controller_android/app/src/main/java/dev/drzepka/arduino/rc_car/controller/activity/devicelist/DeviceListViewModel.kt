package dev.drzepka.arduino.rc_car.controller.activity.devicelist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import dev.drzepka.arduino.rc_car.controller.Utils
import dev.drzepka.arduino.rc_car.controller.bluetooth.finder.BluetoothDeviceFinder
import dev.drzepka.arduino.rc_car.controller.bluetooth.finder.MockBluetoothDeviceFinder
import dev.drzepka.arduino.rc_car.controller.bluetooth.finder.RealBluetoothDeviceFinder
import dev.drzepka.arduino.rc_car.controller.model.BluetoothDeviceData

class DeviceListViewModel(application: Application) : AndroidViewModel(application),
    BluetoothDeviceFinder.BluetoothDeviceListener {
    val deviceList = MutableLiveData<ArrayList<BluetoothDeviceData>>(ArrayList())
    val state = MutableLiveData(State.READY)

    private val finder =
        if (!Utils.isEmulator()) RealBluetoothDeviceFinder(application) else MockBluetoothDeviceFinder()

    init {
        finder.listener = this
    }

    fun startSearchingDevices() {
        state.value = State.SEARCHING
        finder.start()
    }

    fun stopSearchingDevices() {
        state.value = State.DONE
        finder.stop()
    }

    override fun onDeviceFound(deviceData: BluetoothDeviceData) {
        deviceList.value!!.add(deviceData)
        deviceList.value = deviceList.value
    }

    override fun onSearchStarted() {
        deviceList.value = ArrayList()
    }

    override fun onSearchCompleted() {
        stopSearchingDevices()
    }

    enum class State {
        READY, SEARCHING, DONE
    }
}