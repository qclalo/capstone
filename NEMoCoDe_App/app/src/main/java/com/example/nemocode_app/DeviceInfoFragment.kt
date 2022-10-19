package com.example.nemocode_app

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController

class DeviceInfoFragment : Fragment() {

    private var userNameTextViews : MutableMap<String, TextView> = mutableMapOf()
    private var deviceIdTextViews : MutableMap<String, TextView> = mutableMapOf()
    private var severityTextViews : MutableMap<String, TextView> = mutableMapOf()
    private var deviceButtons : MutableMap<String, Button> = mutableMapOf()
    private var severityIcons : MutableMap<String, ImageView> = mutableMapOf()
    private var devices: MutableList<Device> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val viewModel = (activity as MainActivity).deviceFragmentViewModel
        if (viewModel.devices.isEmpty()) {
            Log.i("ViewModel", "MainActivity ViewModel is null adding device")
            addDevice(viewModel)
        } else {
            addDevice(viewModel)
        }

        val view = when (this.devices.size) {
            1 -> inflater.inflate(R.layout.fragment_one_device, container, false)
            2 -> inflater.inflate(R.layout.fragment_two_device, container, false)
            3 -> inflater.inflate(R.layout.fragment_three_device, container, false)
            4 -> inflater.inflate(R.layout.fragment_four_device, container, false)
            5 -> inflater.inflate(R.layout.fragment_five_device, container, false)
            6 -> inflater.inflate(R.layout.fragment_six_device, container, false)
            else -> inflater.inflate(R.layout.fragment_one_device, container, false)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initElementLists()
        val deviceIdTextKey = "deviceid_text_"
        val userNameTextKey = "username_text_"
        val severityTextKey = "severity_text_"
        val severityIconKey = "severity_icon_"
        val deviceButtonKey = "device_button_"
        for (i in 0..this.devices.size) {
            this.userNameTextViews[userNameTextKey + (i + 1)]?.text = this.devices[i].userName
            this.deviceIdTextViews[deviceIdTextKey + (i + 1)]?.text = this.devices[i].deviceId.toString()

            val severityTextView: TextView? = this.severityTextViews[severityTextKey + (i + 1)]
            val severityIcon : ImageView? = this.severityIcons[severityIconKey + (i + 1)]
            setSeverity(severityTextView, severityIcon, i)

            val deviceBtn : Button? = this.deviceButtons[deviceButtonKey + (i + 1)]
            deviceBtn?.setOnClickListener {
                cycleSeverities(i)
                setSeverity(severityTextView, severityIcon, i)
            }

            val BackToMenu : Button = view.findViewById(R.id.back_to_menu)
            BackToMenu.setOnClickListener {
                val action = DeviceInfoFragmentDirections.actionDeviceInfoFragmentToOneDeviceFragment()
                view.findNavController().navigate(action)
            }
        }
    }

    private fun addDevice(viewModel : MyViewModel) {
        val device : Device
        if (this.arguments?.get("deviceInfo") != null) {
            val args = (this.arguments?.get("deviceInfo") as Array<String>)
            device = Device(args[0], args[1].toInt(), Severity.HEALTHY)
            viewModel.devices.add(device)
            this.devices = viewModel.devices
        } else {
            this.devices = viewModel.devices
            device = viewModel.devices[0]
        }
        Log.i("ViewModel", "Added device, info: "
                + "userName=" + device.userName
                + ", deviceID=" + device.deviceId)
        Log.i("ViewModel", "Number of devices = " + viewModel.devices.size)
    }

    private fun initElementLists() {
        val vg : ViewGroup = requireView() as ViewGroup
        for (i in 0..vg.childCount) {
            if (vg.getChildAt(i) is androidx.constraintlayout.widget.ConstraintLayout) {
                val cvg : ViewGroup = vg.getChildAt(i) as ViewGroup
                for (j in 0..cvg.childCount) {
                    insertIntoGroup(cvg, j)
                }
            } else {
                insertIntoGroup(vg, i)
            }
        }
    }

    private fun insertIntoGroup(vg : ViewGroup, i : Int) {
        if (vg.getChildAt(i) is Button) {
            val currButton : Button = vg.getChildAt(i) as Button
            val name : String = requireView().resources.getResourceName(currButton.id).split("/")[1]
            if (name.contains("device_button")) {
                this.deviceButtons[name] = currButton
                Log.i("Init", "Added Button=$name to deviceButtons")
            }
        } else if (vg.getChildAt(i) is TextView) {
            val currTextView : TextView = vg.getChildAt(i) as TextView
            val name : String = requireView().resources.getResourceName(currTextView.id).split("/")[1]
            if (name.contains("username_text")) {
                this.userNameTextViews[name] = currTextView
                Log.i("Init", "Added TextView = $name to userNameTextViews")
            } else if (name.contains("deviceid_text")) {
                this.deviceIdTextViews[name] = currTextView
                Log.i("Init", "Added TextView = $name to deviceIdTextViews")
            } else if (name.contains("severity_text")) {
                this.severityTextViews[name] = currTextView
                Log.i("Init", "Added TextView = $name to severityTextViews")
            } else {
                Log.i("Error", "Couldn't find associated text view to id = $name")
            }
        } else if (vg.getChildAt(i) is ImageView) {
            val currIcon : ImageView = vg.getChildAt(i) as ImageView
            val name : String = requireView().resources.getResourceName(currIcon.id).split("/")[1]
            this.severityIcons[name] = currIcon
            Log.i("Init", "Added ImageView=$name to severityIcons")
        }
    }

    private fun setSeverity(severityTextView : TextView?, severityIcon : ImageView?, deviceIndex: Int) {
        if (severityIcon != null) {
            severityIcon.setImageResource(getSeverityIcon(deviceIndex))
            when (this.devices[deviceIndex].severity) {
                Severity.HEALTHY -> ImageViewCompat.setImageTintList(
                    severityIcon,
                    ColorStateList.valueOf(resources.getColor((R.color.healthy_icon_color)))
                )
                Severity.LOW_RISK -> ImageViewCompat.setImageTintList(
                    severityIcon,
                    ColorStateList.valueOf(resources.getColor((R.color.low_risk_icon_color)))
                )
                Severity.MEDIUM_RISK -> ImageViewCompat.setImageTintList(
                    severityIcon,
                    ColorStateList.valueOf(resources.getColor((R.color.medium_risk_icon_color)))
                )
                Severity.HIGH_RISK -> ImageViewCompat.setImageTintList(
                    severityIcon,
                    ColorStateList.valueOf(resources.getColor((R.color.high_risk_icon_color)))
                )
            }
        }
        severityTextView?.text = devices[deviceIndex].getSeverityText()
    }

    private fun getSeverityIcon(deviceIndex : Int) : Int {
        return when (this.devices[deviceIndex].severity) {
            Severity.HEALTHY -> R.drawable.ic_healthy_severity
            else -> R.drawable.ic_risk_severity
        }
    }

    private fun cycleSeverities(deviceIndex: Int) {
        when (this.devices[deviceIndex].severity) {
            Severity.HEALTHY -> this.devices[deviceIndex].severity = Severity.LOW_RISK
            Severity.LOW_RISK -> this.devices[deviceIndex].severity = Severity.MEDIUM_RISK
            Severity.MEDIUM_RISK -> this.devices[deviceIndex].severity = Severity.HIGH_RISK
            Severity.HIGH_RISK -> this.devices[deviceIndex].severity = Severity.HEALTHY
        }
    }
}


