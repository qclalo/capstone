package com.example.nemocode_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController

class OneDeviceFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_one_device, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val noDevicesTextView: TextView = view.findViewById(R.id.no_devices)
//        if (arguments?.get("deviceInfo") != null) {
//            //noDevicesTextView.visibility = View.INVISIBLE
//            noDevicesTextView.text = (arguments?.get("deviceInfo") as Array<*>)[0].toString()
//        }

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
}