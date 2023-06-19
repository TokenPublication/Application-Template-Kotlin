package com.tokeninc.sardis.application_template.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.databinding.FragmentTriggerBinding
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader


class TriggerFragment(val mainActivity: MainActivity) : Fragment() {


    private var _binding: FragmentTriggerBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTriggerBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startDummyParameterUploading() //TODO do it in model, just update UI
    }


    private fun startDummyParameterUploading(){

        // If Parameter is success
        val resultIntent = Intent()
        val bundle = Bundle()
        val assetManager = mainActivity.assets

        var clConfigFile = ""

        try {
            val xmlCLStream: InputStream = assetManager!!.open("custom_emv_cl_config.xml")
            val rCL = BufferedReader(InputStreamReader(xmlCLStream))
            val totalCL = StringBuilder()
            var line: String?
            while (rCL.readLine().also { line = it } != null) {
                totalCL.append(line).append('\n')
            }
            clConfigFile = totalCL.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        bundle.putString("clConfigFile", clConfigFile)
        val bins =
            "[{\"cardRangeStart\":\"1111110000000\",\"cardRangeEnd\":\"1111119999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"C\"}," +
                "{\"cardRangeStart\":\"2222220000000\",\"cardRangeEnd\":\"2222229999999\",\"OwnerShip\":\"NONE\",\"CardType\":\"C\"}," +
                "{\"cardRangeStart\":\"3333330000000\",\"cardRangeEnd\":\"3333339999999\",\"OwnerShip\":\"BRAND\",\"CardType\":\"C\"}]"
        bundle.putString("BINS", bins)
        bundle.putString("AllowedOperations", "{" + "\"QrAllowed\"" + ":" + "1" + "," + "\"KeyInAllowed\"" + ":" + "1" + "}")
        bundle.putString("SupportedAIDs", "[A0000000031010, A0000000041010, A0000000032010]")
        resultIntent.putExtras(bundle)
        mainActivity.setResult(resultIntent)
    }

    /**
     * This is for avoiding memory leak for binding
     */
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}