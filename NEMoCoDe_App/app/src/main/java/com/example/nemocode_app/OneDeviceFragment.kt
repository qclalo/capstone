package com.example.nemocode_app

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
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
import java.lang.Exception

class OneDeviceFragment : Fragment() {

    private lateinit var device: Device

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_one_device, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = (this.arguments?.get("deviceInfo") as Array<String>)
        this.device = Device(args[0], args[1].toInt())

        val userNameTextView: TextView = view.findViewById(R.id.username_text)
        userNameTextView.text = this.device.getUserName()
        val deviceIdTextView: TextView = view.findViewById(R.id.deviceid_text)
        deviceIdTextView.text = this.device.getDeviceId().toString()
        val severityTextView: TextView = view.findViewById(R.id.severity_text)
        severityTextView.text = this.device.getSeverityText()

        val deviceBtn : Button = view.findViewById(R.id.device_button)
        deviceBtn.setOnClickListener {
            cycleSeverities()
            val severityIcon : ImageView = view.findViewById(R.id.severity_icon)
            severityIcon.setImageResource(getSeverityIcon())
            when (this.device.getSeverity()) {
                Severity.HEALTHY -> ImageViewCompat.setImageTintList(severityIcon,
                    ColorStateList.valueOf(resources.getColor((R.color.healthy_icon_color))))
                Severity.LOW_RISK -> ImageViewCompat.setImageTintList(severityIcon,
                    ColorStateList.valueOf(resources.getColor((R.color.low_risk_icon_color))))
                Severity.MEDIUM_RISK -> ImageViewCompat.setImageTintList(severityIcon,
                    ColorStateList.valueOf(resources.getColor((R.color.medium_risk_icon_color))))
                Severity.HIGH_RISK -> ImageViewCompat.setImageTintList(severityIcon,
                    ColorStateList.valueOf(resources.getColor((R.color.high_risk_icon_color))))
            }
            severityTextView.text = this.device.getSeverityText()
        }

        val deleteDeviceBtn : Button = view.findViewById(R.id.delete_device)
        deleteDeviceBtn.setOnClickListener {
            val action = OneDeviceFragmentDirections.actionOneDeviceFragmentToMainMenuFragment()
            findNavController().navigate(action)
        }

        val addDeviceBtn : Button = view.findViewById(R.id.add_device_one)
        addDeviceBtn.setOnClickListener {
            val action = OneDeviceFragmentDirections.actionOneDeviceFragmentToInstructionMenuFragment("OneDevice")
            view.findNavController().navigate(action)
        }
    }

    private fun getSeverityIcon() : Int {
        return when (this.device.getSeverity()) {
            Severity.HEALTHY -> R.drawable.ic_healthy_severity
            else -> R.drawable.ic_risk_severity
        }
    }

    private fun cycleSeverities() {
        when (this.device.getSeverity()) {
            Severity.HEALTHY -> this.device.setSeverity(Severity.LOW_RISK)
            Severity.LOW_RISK -> this.device.setSeverity(Severity.MEDIUM_RISK)
            Severity.MEDIUM_RISK -> this.device.setSeverity(Severity.HIGH_RISK)
            Severity.HIGH_RISK -> this.device.setSeverity(Severity.HEALTHY)
        }
    }
}