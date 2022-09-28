package com.example.nemocode_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController

class MainMenuFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main_menu, container, false)
        return view


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val noDevicesTextView: TextView = view.findViewById(R.id.no_devices)
        if (arguments?.get("deviceInfo") != null) {
            //noDevicesTextView.visibility = View.INVISIBLE
            noDevicesTextView.text = (arguments?.get("deviceInfo") as Array<*>)[0].toString()
        }

        val addDeviceBtn : Button = view.findViewById(R.id.add_device_main)
        addDeviceBtn.setOnClickListener {
            val action = MainMenuFragmentDirections.actionMainMenuFragmentToInstructionMenuFragment()
            view.findNavController().navigate(action)
        }
    }
}