package dev.drzepka.arduino.rc_car.controller.activity.devicelist

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.drzepka.arduino.rc_car.controller.R
import dev.drzepka.arduino.rc_car.controller.activity.controller.ControllerActivity
import dev.drzepka.arduino.rc_car.controller.model.BluetoothDeviceData

class DeviceListActivity : AppCompatActivity() {
    private val controlProgressBar by lazy { findViewById<ProgressBar>(R.id.activity_device_list_control_progress) }
    private val controlText by lazy { findViewById<TextView>(R.id.activity_device_list_control_text) }
    private val controlButton by lazy { findViewById<Button>(R.id.activity_device_list_control_button) }

    private val viewModel: DeviceListViewModel by viewModels()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)

        val recyclerView = findViewById<RecyclerView>(R.id.activity_device_list_recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = BluetoothDeviceAdapter()

        viewModel.deviceList.observe(this) {
            recyclerView.adapter?.notifyDataSetChanged()
        }

        controlButton.setOnClickListener { handleControlBarAction() }
        viewModel.state.observe(this) {
            refreshControlBar()
        }

        refreshControlBar()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopSearchingDevices()
    }

    private fun refreshControlBar() {
        when (viewModel.state.value!!) {
            DeviceListViewModel.State.READY -> {
                controlProgressBar.visibility = View.GONE
                controlText.setText(R.string.activity_device_list_control_begin_text)
                controlButton.setText(R.string.activity_device_list_control_begin_button)
            }
            DeviceListViewModel.State.SEARCHING -> {
                controlProgressBar.visibility = View.VISIBLE
                controlText.setText(R.string.activity_device_list_control_cancel_text)
                controlButton.setText(R.string.activity_device_list_control_cancel_button)
            }
            DeviceListViewModel.State.DONE -> {
                controlProgressBar.visibility = View.GONE
                controlText.setText(R.string.activity_device_list_control_retry_text)
                controlButton.setText(R.string.activity_device_list_control_retry_button)
            }
        }
    }

    private fun handleControlBarAction() {
        when(viewModel.state.value!!) {
            DeviceListViewModel.State.READY -> viewModel.startSearchingDevices()
            DeviceListViewModel.State.SEARCHING -> viewModel.stopSearchingDevices()
            DeviceListViewModel.State.DONE -> viewModel.startSearchingDevices()
        }
    }

    private fun goToController(device: BluetoothDeviceData) {
        viewModel.stopSearchingDevices()

        val intent = Intent(this, ControllerActivity::class.java)
        intent.putExtra(ControllerActivity.EXTRA_DEVICE_MAC, device.mac)
        startActivity(intent)
    }

    inner class BluetoothDeviceAdapter : RecyclerView.Adapter<BluetoothDeviceHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BluetoothDeviceHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.view_list_item, parent, false)
            return BluetoothDeviceHolder(view)
        }

        override fun onBindViewHolder(holder: BluetoothDeviceHolder, position: Int) {
            val deviceData = viewModel.deviceList.value!![position]
            holder.update(deviceData)
        }

        override fun getItemCount(): Int {
            return viewModel.deviceList.value?.size ?: 0
        }
    }

    inner class BluetoothDeviceHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val text1 = view.findViewById<TextView>(android.R.id.text1)
        private val text2 = view.findViewById<TextView>(android.R.id.text2)

        fun update(device: BluetoothDeviceData) {
            text1.text = device.name
            text2.text = device.mac

            itemView.setOnClickListener {
                goToController(device)
            }
        }
    }
}