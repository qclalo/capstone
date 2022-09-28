package com.example.nemocode_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class InstructionMenuFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_instruction_menu, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addDeviceConfirmBtn : Button = view.findViewById(R.id.add_device_confirm)
        addDeviceConfirmBtn.setOnClickListener {
            findNavController().navigate(R.id.action_instructionMenuFragment_to_mainMenuFragment)
        }

        val cancelBtn : Button = view.findViewById(R.id.cancel_button)
        cancelBtn.setOnClickListener {
            findNavController().navigate(R.id.action_instructionMenuFragment_to_mainMenuFragment)
        }
    }
}