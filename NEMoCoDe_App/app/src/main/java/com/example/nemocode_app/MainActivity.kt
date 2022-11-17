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
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.util.*

// Defines several constants used when transmitting messages between the
// service and the UI.
const val MESSAGE_READ: Int = 0
const val MESSAGE_WRITE: Int = 1
const val MESSAGE_TOAST: Int = 2

class MainActivity : AppCompatActivity() {
    val deviceFragmentViewModel: MyViewModel = MyViewModel()
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var connectThread : ConnectThread
    private lateinit var connectedThread: ConnectedThread
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

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onDestroy() {
        super.onDestroy()
        Log.i("Lifecycle", "In onDestroy in MainActivity")
        unregisterReceiver(bluetoothReceiver)
        unregisterReceiver(discoverDeviceReceiver)
        this.connectThread.cancel()
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
    @SuppressLint("MissingPermission")
    fun connectDevice(device: BluetoothDevice) {
        this.connectThread = ConnectThread(device)
        this.connectThread.run()
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

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)
    private inner class ConnectThread(device: BluetoothDevice) : Thread() {

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            val deviceUuid : UUID = UUID.fromString("3f6c999f-92d2-411b-b756-3212dddf83b7")
            for (uuidFound in device.uuids.iterator()) {
                if (deviceUuid == uuidFound.uuid) {
                    Log.i("Bluetooth", "Found matching uuid in socket")
                }
            }
            device.createRfcommSocketToServiceRecord(deviceUuid)
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
        override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery()

            val handler : Handler = Handler(Looper.getMainLooper())

            mmSocket?.let { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                socket.connect()
                Log.i("Bluetooth", "Connection to bluetooth device succeeded");

                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                connectedThread = ConnectedThread(handler, mmSocket!!)
                connectedThread.run()
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e("Error", "Could not close the client socket", e)
            }
        }
    }

    private inner class ConnectedThread(private val handler : Handler,
        private val mmSocket: BluetoothSocket) : Thread() {

        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        override fun run() {
            var numBytes: Int // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                numBytes = try {
                    mmInStream.read(mmBuffer)
                } catch (e: IOException) {
                    Log.d("Bluetooth", "Input stream was disconnected", e)
                    break
                }

                // Send the obtained bytes to the UI activity.
                val readMsg = handler.obtainMessage(MESSAGE_READ, numBytes, -1, mmBuffer)
                readMsg.sendToTarget()
            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e("Bluetooth", "Error occurred when sending data", e)

                // Send a failure message back to the activity.
                val writeErrorMsg = handler.obtainMessage(MESSAGE_TOAST)
                val bundle = Bundle().apply {
                    putString("toast", "Couldn't send data to the other device")
                }
                writeErrorMsg.data = bundle
                handler.sendMessage(writeErrorMsg)
                return
            }

            // Share the sent message with the UI activity.
            val writtenMsg = handler.obtainMessage(
                MESSAGE_WRITE, -1, -1, mmBuffer)
            writtenMsg.sendToTarget()
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e("Bluetooth", "Could not close the connect socket", e)
            }
        }
    }
}