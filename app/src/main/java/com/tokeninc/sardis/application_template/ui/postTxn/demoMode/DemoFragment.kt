package com.tokeninc.sardis.application_template.ui.postTxn.demoMode

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.databinding.FragmentDemoBinding
import com.tokeninc.sardis.application_template.utils.BaseFragment

class DemoFragment() : BaseFragment() {

    private lateinit var binding: FragmentDemoBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentDemoBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPreferences = safeActivity.getSharedPreferences("myprefs", Context.MODE_PRIVATE)
        val isEnabled = sharedPreferences.getBoolean("demo_mode", false)
        val demoStatus: SwitchCompat = binding.demoSwitch
        demoStatus.isChecked = isEnabled
        demoStatus.setOnClickListener {
            changeMode(sharedPreferences, demoStatus.isChecked)
        }
    }

    private fun changeMode(sharedPreferences: SharedPreferences, isEnabled: Boolean) {
        val editor = sharedPreferences.edit()
        Log.i("Demo Mode", "Enable: $isEnabled")
        editor.putBoolean("demo_mode", isEnabled)
        editor.apply()
    }
}
