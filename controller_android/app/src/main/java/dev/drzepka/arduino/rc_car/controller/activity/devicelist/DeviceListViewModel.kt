package dev.drzepka.arduino.rc_car.controller.activity.devicelist

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
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

        state.observeForever {
            Log.i(TAG, "Current state: $it")
        }
    }

    override fun onCleared() {
        stopSearchingDevices()
    }

    fun startSearchingDevices() {
        if (!hasPermissions()) {
            state.value = State.NO_PERMISSION
            return
        }

        if (finder.start())
            state.value = State.SEARCHING
        else
            state.value = State.ERROR
    }

    fun stopSearchingDevices() {
        state.value = State.DONE
        finder.stop()
    }

    fun getRequiredPermissions(): List<String> {
        val permissions = ArrayList<String>()
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

        return permissions
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

    private fun hasPermissions(): Boolean {
        if (Utils.isEmulator())
            return true

        val application = getApplication<Application>()

        val requiredPermissions = getRequiredPermissions()
        val missingPermissions = requiredPermissions.filter {
            application.checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty())
            Log.i(TAG, "There are permissions missing: $missingPermissions")

        return missingPermissions.isEmpty()
    }

    enum class State {
        READY, SEARCHING, DONE, ERROR, NO_PERMISSION
    }

    companion object {
        private const val TAG = "DeviceListViewModel"
    }
}