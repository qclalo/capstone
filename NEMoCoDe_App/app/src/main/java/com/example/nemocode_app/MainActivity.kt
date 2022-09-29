package com.example.nemocode_app

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.lang.IllegalArgumentException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}

class Device constructor(userName: String, deviceId: Number) {
    private lateinit var userName : String
    private lateinit var deviceId : Number
    private lateinit var severity: Severity

    init {
        this.userName = userName
        this.deviceId = deviceId
        this.severity = Severity.HEALTHY
    }

    fun getUserName() : String {
        return this.userName
    }

    fun getDeviceId() : Number {
        return this.deviceId
    }

    fun getSeverity() : Severity {
        return this.severity
    }

    fun getSeverityText() : String {
        return when (this.severity) {
            Severity.HEALTHY -> "User is healthy!"
            Severity.LOW_RISK -> "User is Low Risk"
            Severity.MEDIUM_RISK -> "User is Medium Risk proceed with caution"
            Severity.HIGH_RISK -> "Warning: User is High Risk provide help immediately"
        }
    }

    fun setSeverity(severity: Severity) {
        this.severity = severity
    }

    private fun startSession() {
        //TODO: IMPLEMENT FUNCTION
    }
}

enum class Severity {
    HEALTHY,LOW_RISK,MEDIUM_RISK,HIGH_RISK
}