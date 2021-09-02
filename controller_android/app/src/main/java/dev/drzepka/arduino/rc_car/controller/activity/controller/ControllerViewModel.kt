package dev.drzepka.arduino.rc_car.controller.activity.controller

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import dev.drzepka.arduino.rc_car.controller.Utils
import dev.drzepka.arduino.rc_car.controller.bluetooth.connection.MockConnectionManager
import dev.drzepka.arduino.rc_car.controller.bluetooth.connection.RealConnectionManager
import kotlin.concurrent.thread

class ControllerViewModel(application: Application) : AndroidViewModel(application) {

    var mac: String? = null
        private set
    var errorMessage: String? = null
        private set

    val state = MutableLiveData<State>()

    private val manager =
        if (!Utils.isEmulator()) RealConnectionManager(application) else MockConnectionManager()

    fun connect(mac: String) {
        if (this.mac == mac || state.value != null)
            return

        this.mac = mac
        doConnect()
    }

    fun reconnect() {
        if (state.value != State.ERROR)
            return

        doConnect()
    }

    override fun onCleared() {
        manager.disconnect()
    }

    fun setJoystickPosition(speed: Int, direction: Int) {
        // todo:
    }

    private fun doConnect() {
        state.value = State.CONNECTING

        thread {
            establishConnection()
        }
    }

    private fun establishConnection() {
        try {
            manager.connect(mac!!)
            state.postValue(State.CONNECTED)
        } catch (e: Exception) {
            e.printStackTrace()
            errorMessage = e.message
            state.postValue(State.ERROR)
        }
    }

    enum class State {
        CONNECTING,
        CONNECTED,
        ERROR
    }
}