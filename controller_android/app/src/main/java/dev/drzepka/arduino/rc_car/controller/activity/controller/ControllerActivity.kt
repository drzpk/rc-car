package dev.drzepka.arduino.rc_car.controller.activity.controller

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import dev.drzepka.arduino.rc_car.controller.R
import dev.drzepka.arduino.rc_car.controller.widget.Joystick
import kotlin.math.floor

class ControllerActivity : AppCompatActivity(), Joystick.PositionListener {
    private val valueX by lazy { findViewById<TextView>(R.id.activity_controller_value_x) }
    private val valueY by lazy { findViewById<TextView>(R.id.activity_controller_value_y) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_controller)

        findViewById<Joystick>(R.id.activity_controller_joystick).apply {
            positionListener = this@ControllerActivity
        }
    }

    override fun onPositionChanged(x: Float, y: Float) {
        valueX.text = floor(x * 100).toInt().toString()
        valueY.text = floor(y * 100).toInt().toString()
    }
}