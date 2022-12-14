package com.example.nemocode_app

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import androidx.lifecycle.ViewModel
import java.io.IOException

class MyViewModel : ViewModel() {
    var devices: MutableList<Device> = mutableListOf()
    var btDevices : MutableMap<String, BluetoothDevice> = mutableMapOf()
}

class Device constructor(var userName: String, var deviceId: Int, var severity: Severity) {
    var macAddress = ""
    var accelerationData : MutableList<Double> = mutableListOf()

    fun getSeverityText(): String {
        return when (this.severity) {
            Severity.HEALTHY -> "User is healthy!"
            Severity.LOW_RISK -> "User is Low Risk"
            Severity.MEDIUM_RISK -> "User is Medium Risk proceed with caution"
            Severity.HIGH_RISK -> "User is High Risk provide help immediately"
        }
    }

    private fun startSession() {
        //TODO: IMPLEMENT FUNCTION
    }
}

enum class Severity {
    HEALTHY, LOW_RISK, MEDIUM_RISK, HIGH_RISK
}

