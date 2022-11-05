package com.example.nemocode_app

import android.Manifest
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewDebug
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText

class InstructionMenuFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_instruction_menu, container, false)
        Log.i("Lifecycle", "In onCreateView in InstructionMenuFragment")
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("Lifecycle", "In onViewCreated in InstructionMenuFragment")

        val addDeviceConfirmBtn : Button = view.findViewById(R.id.add_device_confirm)
        addDeviceConfirmBtn.setOnClickListener {
            val deviceInfo = getDeviceInfo(view)
            if (checkValidInfoEntered(deviceInfo)) {
                val action = InstructionMenuFragmentDirections.actionInstructionMenuFragmentToOneDeviceFragment(deviceInfo)
                findNavController().navigate(action)
            } else {
                val invalidInfoToast = Toast.makeText(context, "Error: Invalid device info entered", Toast.LENGTH_SHORT)
                invalidInfoToast.show()
            }
        }

        val cancelBtn : Button = view.findViewById(R.id.cancel_button)
        cancelBtn.setOnClickListener {
            val caller = this.arguments?.get("caller").toString()
            if (caller == "OneDevice") {
                val action = InstructionMenuFragmentDirections.actionInstructionMenuFragmentToOneDeviceFragment()
                findNavController().navigate(action)
            } else {
                val action = InstructionMenuFragmentDirections.actionInstructionMenuFragmentToMainMenuFragment()
                findNavController().navigate(action)
            }
        }
    }

    // Get the contents of all TextInputEditText boxes and return them in a string array
    private fun getDeviceInfo(view: View) : Array<String> {
        val vg : ViewGroup = view as ViewGroup
        val deviceInfoList : MutableList<String> = MutableList(0) {i -> ""}
        var cnt = 0
        for (i in 0..vg.childCount) {
            if (vg.getChildAt(i) is TextInputEditText) {
                val currInputEditText : TextInputEditText = vg.getChildAt(i) as TextInputEditText
                deviceInfoList.add(currInputEditText.text.toString())
                cnt += 1
            }
        }
        return deviceInfoList.toTypedArray()
    }

    // Return false if any of the values in deviceInfo are empty
    private fun checkValidInfoEntered(deviceInfo: Array<String>) : Boolean {
        for (value in deviceInfo) {
            if (value == "") {
                return false
            }
        }
        return true
    }

    private fun getBluetoothPermissions(): BluetoothAdapter? {
        // BLUETOOTH:
        val context : Context = requireContext()
        val bluetoothManager: BluetoothManager? = getSystemService(context, BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
        if (bluetoothAdapter == null) {
            Log.i("Error", "Device does not support bluetooth")
            return null
        }
        if (!bluetoothAdapter.isEnabled) {
            val getResult = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()) {
                if(it.resultCode == Activity.RESULT_OK){
                    val value = it.data?.getStringExtra("input")
                }
            }
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            getResult.launch(enableBtIntent)
        }
        return bluetoothAdapter
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(BLUETOOTH_CONNECT)
    private fun connectBluetoothDevice(bluetoothAdapter : BluetoothAdapter) : Set<BluetoothDevice>? {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            val deviceHardwareAddress = device.address // MAC address
            Log.i("Bluetooth", "Connected to $deviceName with address $deviceHardwareAddress")
        }

        return pairedDevices
    }
}
