package dev.drzepka.arduino.rc_car.controller.activity.controller

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import dev.drzepka.arduino.rc_car.controller.Utils
import dev.drzepka.arduino.rc_car.controller.bluetooth.connection.MockConnectionManager
import dev.drzepka.arduino.rc_car.controller.bluetooth.connection.RealConnectionManager
import dev.drzepka.arduino.rc_car.controller.model.ControlMessage
import kotlin.concurrent.thread

class ControllerViewModel(application: Application) : AndroidViewModel(application) {

    var mac: String? = null
        private set
    var errorMessage: String? = null
        private set

    val state = MutableLiveData<State>()

    private val manager =
        if (!Utils.isEmulator()) RealConnectionManager(application) else MockConnectionManager()
    private val sender = MessageSender(manager)

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
        sender.stop()
        manager.disconnect()
    }

    fun setJoystickPosition(speed: Int, direction: Int) {
        val message = ControlMessage(speed, direction, brake = false, horn = false)
        sender.setMessage(message)
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
            sender.start()
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