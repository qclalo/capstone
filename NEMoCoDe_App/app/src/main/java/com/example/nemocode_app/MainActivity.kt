package com.example.nemocode_app

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import java.io.IOException
import java.net.Socket
import java.util.*

class MainActivity : AppCompatActivity() {
    val deviceFragmentViewModel: MyViewModel = MyViewModel()
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothManager: BluetoothManager
    private val REQUEST_ACCESS_COARSE_LOCATION = 101


    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.i("Lifecycle", "In onSaveInstanceState in MainActivity")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("Lifecycle", "In onDestroy in MainActivity")
        unregisterReceiver(bluetoothReceiver)
        unregisterReceiver(discoverDeviceReceiver)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun enableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            bluetoothAdapter.enable()
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(intent)

            val intentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            registerReceiver(bluetoothReceiver, intentFilter)
        }
        Log.i("Bluetooth", "Bluetooth enabled")
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun getPairedDevices() {
        val btDevices = bluetoothAdapter.bondedDevices
        Log.d("Bluetooth", "Found ${btDevices.size} paired devices")
        Log.d("Bluetooth", "Paired devices: $btDevices")
        for (device in btDevices) {
            if (device.name.contains("nemocode")) {
                deviceFragmentViewModel.btDevices[device.name] = device
            }
            Log.d("Bluetooth", "Found paired bt device: " + device.name + "   " + device.address + "   " + device.bondState)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
    fun discoverDevices() {
        when(ContextCompat.checkSelfPermission(baseContext, Manifest.permission.ACCESS_COARSE_LOCATION)){
            PackageManager.PERMISSION_DENIED -> {
                if(ContextCompat.checkSelfPermission(baseContext,Manifest.permission.ACCESS_COARSE_LOCATION)!=
                    PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_ACCESS_COARSE_LOCATION)
                }
            }
            PackageManager.PERMISSION_GRANTED -> {
                Log.d("Bluetooth","Location permission Granted")
            }
        }
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(discoverDeviceReceiver, filter)
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        bluetoothAdapter.startDiscovery()
    }

    private val discoverDeviceReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            var action = ""
            if (intent != null) {
                action = intent.action.toString()
            }
            when (action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    Log.d("Bluetooth", "STATE CHANGED")
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d("Bluetooth", "Discovery Started")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d("Bluetooth", "Discovery Finished")
                }
                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent?.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null) {
                        if (device.name.contains("nemocode")) {
                            deviceFragmentViewModel.btDevices[device.name] = device
                        }
                        Log.d("Bluetooth", "Bluetooth device found ${device.name}  ${device.address}")
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectDevice(device: BluetoothDevice) {
        val macAddr = device.address
        val uuid : UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        val socket : BluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
        try {
            socket.connect()
        } catch (e : IOException) {
            Log.i("Bluetooth", "Could not connect to ${device.name} with addr $macAddr")
            return
        }
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if(action==BluetoothAdapter.ACTION_STATE_CHANGED){
                when(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR)){
                    BluetoothAdapter.STATE_OFF->{
                        Log.d("Bluetooth","Bluetooth off")
                    }
                    BluetoothAdapter.STATE_TURNING_OFF->{
                        Log.d("Bluetooth","Bluetooth turning off")
                    }
                    BluetoothAdapter.STATE_ON-> {
                        Log.d("Bluetooth","Bluetooth On")
                    }
                    BluetoothAdapter.STATE_TURNING_ON->{
                        Log.d("Bluetooth","Bluetooth turning on")
                    }
                }
            }
        }
    }

}