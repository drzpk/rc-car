package dev.drzepka.arduino.rc_car.controller.model

import kotlin.experimental.or
import kotlin.math.round

data class ControlMessage(val speed: Int, val direction: Int, val brake: Boolean) {

    /**
     * Rounds speed and direction to given precision.
     * An example for precision 5:
     * ```
     * 22 -> 20
     * 25 -> 25
     * 27.4 -> 25
     * 27.5 -> 30
     * ```
     */
    fun withPrecision(precision: Int): ControlMessage {
        val roundedSpeed = (precision * round(speed.toFloat() / precision + 0.5f)).toInt()
        val roundedDirection = (precision * round(direction.toFloat() / precision + 0.5f)).toInt()

        return ControlMessage(roundedSpeed, roundedDirection, brake)
    }

    fun serialize(): ByteArray {
        val array = ArrayList<Byte>()
        array.addAll(MESSAGE_INDICATOR)
        array.add(speed.toByte())
        array.add(direction.toByte())

        val flags = (0).toByte() or brake.compareTo(false).toByte()
        array.add(flags)

        return array.toByteArray()
    }

    companion object {
        private val MESSAGE_INDICATOR = listOf(
            (0xab).toByte(),
            (0xd1).toByte()
        )
    }
}