package dev.drzepka.arduino.rc_car.controller.activity.controller

import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dev.drzepka.arduino.rc_car.controller.R
import dev.drzepka.arduino.rc_car.controller.activity.settings.SettingsActivity
import dev.drzepka.arduino.rc_car.controller.widget.Joystick
import kotlin.math.floor

class ControllerActivity : AppCompatActivity(), Joystick.PositionListener {
    private val speedValue by lazy { findViewById<TextView>(R.id.activity_controller_value_speed) }
    private val directionValue by lazy { findViewById<TextView>(R.id.activity_controller_value_direction) }

    private val viewModel: ControllerViewModel by viewModels()

    private var loadingDialog: Dialog? = null
    private var errorDialog: Dialog? = null
    private var startForResult: ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_controller)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        findViewById<Joystick>(R.id.activity_controller_joystick).apply {
            positionListener = this@ControllerActivity
        }

        viewModel.state.observe(this) {
            onStateChanged(it!!)
        }

        val mac = intent.getStringExtra(EXTRA_DEVICE_MAC)!!
        viewModel.connect(mac)

        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            viewModel.notifySettingsChanged()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        MenuInflater(this).inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            finish()

        if (item.itemId == R.id.menu_action_settings) {
            startForResult?.launch(Intent(this, SettingsActivity::class.java))
        }

        return true
    }

    override fun onPositionChanged(x: Float, y: Float) {
        val speed = floor(x * 100).toInt()
        val direction = floor(y * 100).toInt()
        viewModel.setJoystickPosition(speed, direction)

        speedValue.text = speed.toString()
        directionValue.text = direction.toString()
    }

    private fun onStateChanged(state: ControllerViewModel.State) {
        when (state) {
            ControllerViewModel.State.CONNECTING -> showLoadingDialog()
            ControllerViewModel.State.CONNECTED -> showConnectedToast()
            ControllerViewModel.State.ERROR -> showErrorDialog()
        }
    }

    private fun showLoadingDialog() {
        errorDialog?.dismiss()

        val title = getString(R.string.activity_controller_loading_dialog_title)
        val text = getString(R.string.activity_controller_loading_dialog_text, viewModel.mac)
        loadingDialog = ProgressDialog.show(this, title, text, true)
    }

    private fun showConnectedToast() {
        Toast.makeText(this, R.string.activity_controller_connected_toast, Toast.LENGTH_SHORT)
            .show()
        loadingDialog?.dismiss()
        errorDialog?.dismiss()
    }

    private fun showErrorDialog() {
        loadingDialog?.dismiss()

        val message = getString(
            R.string.activity_controller_error_dialog_text,
            viewModel.mac,
            viewModel.errorMessage
        )

        val builder = AlertDialog.Builder(this)
            .setTitle(R.string.activity_controller_error_dialog_title)
            .setMessage(message)

        builder.setNeutralButton(R.string.activity_controller_error_dialog_button_retry) { dialogInterface, _ ->
            dialogInterface.dismiss()
            viewModel.reconnect()
        }

        builder.setNegativeButton(R.string.activity_controller_error_dialog_button_close) { _, _ ->
            finish()
        }

        errorDialog = builder.show()
    }

    companion object {
        const val EXTRA_DEVICE_MAC = "deviceMac"
    }
}