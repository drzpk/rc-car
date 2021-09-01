package dev.drzepka.arduino.rc_car.controller.activity.controller

import android.os.Bundle
import android.widget.TextView
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import dev.drzepka.arduino.rc_car.controller.R
import dev.drzepka.arduino.rc_car.controller.widget.Joystick
import kotlin.math.floor

class ControllerActivity : AppCompatActivity(), Joystick.PositionListener {
    private val speedValue by lazy { findViewById<TextView>(R.id.activity_controller_value_speed) }
    private val directionValue by lazy { findViewById<TextView>(R.id.activity_controller_value_direction) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_controller)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        findViewById<Joystick>(R.id.activity_controller_joystick).apply {
            positionListener = this@ControllerActivity
        }
    }

    override fun onPositionChanged(x: Float, y: Float) {
        speedValue.text = floor(x * 100).toInt().toString()
        directionValue.text = floor(y * 100).toInt().toString()
    }
}