package com.example.nemocode_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class MainMenuFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main_menu, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addDeviceBtn : Button = view.findViewById(R.id.add_device_main)
        addDeviceBtn.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenuFragment_to_instructionMenuFragment)
        }
    }
}