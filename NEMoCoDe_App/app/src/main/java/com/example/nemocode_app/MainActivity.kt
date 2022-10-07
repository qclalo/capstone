package com.example.nemocode_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.core.content.PackageManagerCompat.LOG_TAG
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

class MainActivity : AppCompatActivity() {
    val deviceFragmentViewModel : MyViewModel = MyViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.i("Lifecycle", "In onSaveInstanceState in MainActivity")
    }
}

class MyViewModel : ViewModel() {
    var devices : MutableList<Device> = mutableListOf()
}

class Device constructor(var userName: String, var deviceId: Int, var severity: Severity) {
    fun getSeverityText() : String {
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
    HEALTHY,LOW_RISK,MEDIUM_RISK,HIGH_RISK
}