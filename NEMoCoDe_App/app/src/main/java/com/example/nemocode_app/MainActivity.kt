package com.example.nemocode_app

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import android.provider.Settings.Global.DEVICE_NAME
import android.provider.SyncStateContract
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Method

// Defines several constants used when transmitting messages between the
// service and the UI.
const val MESSAGE_READ: Int = 0
const val MESSAGE_WRITE: Int = 1
const val MESSAGE_TOAST: Int = 2

class MainActivity : AppCompatActivity() {

    val deviceFragmentViewModel: MyViewModel = MyViewModel()
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var socket: BluetoothSocket
    private lateinit var bluetoothHandler : Handler
    private lateinit var connectedThread : ConnectedThread
    private val REQUEST_ACCESS_COARSE_LOCATION = 101

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Intent.FLAG_ACTIVITY_NEW_TASK
        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        bluetoothHandler = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MESSAGE_WRITE -> {
                        val writeBuf = msg.obj as ByteArray
                        // construct a string from the buffer
                        val writeMessage = String(writeBuf)
                        Log.d("Bluetooth", "Write Message = $writeMessage")
//                        mConversationArrayAdapter.add("Me:  $writeMessage")
                    }
                    MESSAGE_READ -> {
                        val readBuf = msg.obj as ByteArray
                        // construct a string from the valid bytes in the buffer
                        val readMessage = String(readBuf, 0, msg.arg1)
                        Log.d("Bluetooth", "Read Message = $readMessage")
//                        mConversationArrayAdapter.add(mConnectedDeviceName.toString() + ":  " + readMessage)
                    }
                }
            }
        }
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
        connectedThread.cancel()
        socket.close()
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
        Log.d("Bluetooth", "Bluetooth enabled")
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun getPairedDevices() {
        val btDevices = bluetoothAdapter.bondedDevices
        Log.d("Bluetooth", "Found ${btDevices.size} paired devices")
        Log.d("Bluetooth", "Paired devices: $btDevices")
        for (device in btDevices) {
            Log.d("Bluetooth", "Found paired bt device: " + device.name + "   " + device.address + "   " + device.bondState)
            if (device.name.contains("nemocode")) {
                deviceFragmentViewModel.btDevices[device.name] = device
                //connectDevice(device)
            }
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
        @RequiresApi(Build.VERSION_CODES.S)
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
                    if (device != null && device.name != null) {
                        Log.d("Bluetooth", "Bluetooth device found ${device.name}  ${device.address}")
                        if (device.name.contains("nemocode")) {
                            deviceFragmentViewModel.btDevices[device.name] = device
                            //connectDevice(device)
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    fun connectDevice(device: BluetoothDevice) {
        Log.d("Bluetooth", "Connecting to ${device.name}")
        val port = 1
        val m : Method = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
        socket = m.invoke(device, port) as BluetoothSocket
        bluetoothAdapter.cancelDiscovery()
        socket.connect()
        Log.d("Bluetooth", "Successfully connected to ${device.name} on port $port")
        //this.monitorConnection(socket)
        //socket.outputStream.write("Hello World".toByteArray())
        //connectedThread = ConnectedThread(socket, this.bluetoothHandler);

    }

    fun monitorConnection() {
        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        connectedThread.start()
        connectedThread.write("Hello World".toByteArray())
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


    private inner class ConnectedThread(private val mmSocket: BluetoothSocket,
                                        private val handler : Handler) : Thread() {

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
            val writtenMsg = handler.obtainMessage(MESSAGE_WRITE, -1, -1, mmBuffer)
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
