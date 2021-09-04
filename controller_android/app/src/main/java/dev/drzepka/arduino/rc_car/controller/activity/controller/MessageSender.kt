package dev.drzepka.arduino.rc_car.controller.activity.controller

import android.os.Handler
import android.os.Looper
import android.util.Log
import dev.drzepka.arduino.rc_car.controller.bluetooth.connection.ConnectionManager
import dev.drzepka.arduino.rc_car.controller.model.ControlMessage
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

class MessageSender(private val connectionManager: ConnectionManager) {
    private val message = AtomicReference<ControlMessage>()
    private val working = AtomicBoolean(false)

    init {
        message.set(ControlMessage(0, 0, brake = false, horn = false, 0, 0))
    }

    fun setMessage(message: ControlMessage) {
        this.message.set(message)
    }

    fun start() {
        if (working.get())
            return

        Log.i(TAG, "Starting sender")
        working.set(true)
        thread(block = this::worker)
    }

    fun stop() {
        Log.i(TAG, "Stopping sender")
        working.set(false)
    }

    private fun worker() {
        Log.i(TAG, "Starting worker")
        var previousMessage: ControlMessage? = null
        var currentPingStatus = 0

        while (working.get()) {
            val currentMessage = message.get()
            val messageChanged = currentMessage != previousMessage

            if (messageChanged)
                currentPingStatus = -1

            previousMessage = currentMessage
            currentPingStatus = (currentPingStatus + 1) % PING_INTERVAL

            val send = currentPingStatus == 0 && currentMessage != null
            if (send && connectionManager.isConnected) {
                Handler(Looper.getMainLooper()).post {
                    connectionManager.sendData(message.get().serialize())
                }
            }

            Thread.sleep(SLEEP_TIME)
        }
    }

    companion object {
        private const val TAG = "MessageSender"
        private const val SLEEP_TIME = 200L
        private const val PING_INTERVAL = 4
    }
}