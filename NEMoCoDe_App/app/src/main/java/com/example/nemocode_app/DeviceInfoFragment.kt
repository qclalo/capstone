package com.example.nemocode_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController

class DeviceInfoFragment : Fragment() {

    private lateinit var device: Device;

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val viewModel = (activity as MainActivity).deviceFragmentViewModel
        val device_id = this.arguments?.get("device_id") as Int
        device = viewModel.devices[device_id]

        val view = inflater.inflate(R.layout.device_info, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userNameTextView : TextView = view.findViewById(R.id.user_information)
        userNameTextView.text = device.userName
        val severityTextView: TextView = view.findViewById(R.id.severity_text_info)
        severityTextView.text = device.getSeverityText()
        val deviceIdTextView : TextView = view.findViewById(R.id.deviceid_text_info)
        deviceIdTextView.text = device.deviceId.toString()

        val backToMenu : Button = view.findViewById(R.id.back_to_menu)
        backToMenu.setOnClickListener {
            val action = DeviceInfoFragmentDirections.actionDeviceInfoFragmentToOneDeviceFragment()
            view.findNavController().navigate(action)
        }
    }

}


