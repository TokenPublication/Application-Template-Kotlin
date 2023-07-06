package com.tokeninc.sardis.application_template.ui.activation

import com.tokeninc.sardis.application_template.ui.MenuItem
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.token.uicomponents.CustomInput.CustomInputFormat
import com.token.uicomponents.CustomInput.EditTextInputType
import com.token.uicomponents.CustomInput.InputListFragment
import com.token.uicomponents.CustomInput.InputValidator
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.databinding.FragmentSettingsBinding
import org.apache.commons.lang3.StringUtils


/**
 * This fragment is for Setting Configuration, it depends on Activation Database
 */
class SettingsFragment(private val mainActivity: MainActivity,
                       private val activationViewModel: ActivationViewModel,
                       private val intent: Intent) : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private var isBankActivateAction = true

    private var terminalId: String? = null
    private var merchantId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isBankActivateAction = intent.action != null && intent.action.equals("Activate_Bank")
        if (isBankActivateAction) {
            terminalId = intent.getStringExtra("terminalID")
            merchantId = intent.getStringExtra("merchantID")
        } else {
            showMenu()
        }
    }

    /**
     * This prepares menu, when user clicks Setup, Terminal ID & Merchant ID fragment will be opened
     * If user clicks Host Settings user can change IP Port configuration with opening page.
     */
    private fun showMenu(){
        val menuItems = mutableListOf<IListMenuItem>()
        menuItems.add(MenuItem("Setup", {
            addTidMidFragment()
        }))
        menuItems.add(MenuItem("Host Settings", {
            addIPFragment()
        }))
        activationViewModel.menuItemList = menuItems
        activationViewModel.replaceFragment(mainActivity)
    }

    /**
     * This prepares IP, Port fragment with validator, it shows initial IP, Port number on the screen
     * If user changes their values, these values are updated in database
     */
    private fun addIPFragment(){

        val inputList = mutableListOf<CustomInputFormat>()
        inputList.add(CustomInputFormat("IP",EditTextInputType.IpAddress,
        null, "Invalid IP!", InputValidator {
                validate(it)
            }))

        inputList.add(CustomInputFormat(
            "Port", EditTextInputType.Number, 4, "Invalid Port!"
        ) { customInputFormat -> customInputFormat.text.length >= 2 && customInputFormat.text.toInt() > 0 })

         activationViewModel.hostIP.observe(mainActivity) {
             inputList[0].text = it
         }
        activationViewModel.hostPort.observe(mainActivity){
            inputList[1].text = it
        }
        val hostFragment = InputListFragment.newInstance(inputList, "Save",
            InputListFragment.ButtonListener{
                val ipNo = inputList[0].text
                val portNo = inputList[1].text
                activationViewModel.hostIP.observe(mainActivity){//to get the current Val
                    activationViewModel.updateConnection(ipNo, portNo,it)
                }
                mainActivity.popFragment()
            })

        mainActivity.addFragment(hostFragment as Fragment)
    }

    /**
     * This is for validating the input.
     */
    private fun validate(customInputFormat: CustomInputFormat): Boolean {
        val text = customInputFormat.text
        var isValid: Boolean =
            StringUtils.countMatches(text, ".") === 3 && text.split("\\.").toTypedArray().size == 4
        if (isValid) {
            val array = text.split("\\.").toTypedArray()
            var index = 0
            while (isValid && index < array.size) {
                isValid = StringUtils.isNumeric(array[0])
                index++
            }
        }
        return isValid
    }

    /** It shows a page with a fragment that contains Merchant and Terminal ID inputs
     * These 2 input values comes from Activation database, when user click save button, their values are updating
     * with respect to entering inputs.
     * */
    private fun addTidMidFragment() {
        val inputList = mutableListOf<CustomInputFormat>()
        inputList.add(CustomInputFormat(
            "Merchant No",
            EditTextInputType.Number,
            10,
            "Invalid Merchant No!"
        ) { input -> input.text.length == 10 })

        inputList.add(CustomInputFormat(
            "Terminal No",
            EditTextInputType.Text,
            8,
            "Invalid Terminal No!"
        ) { input -> input.text.length == 8 })

        mainActivity.observeTIDMID()
        inputList[0].text = mainActivity.currentMID
        inputList[1].text = mainActivity.currentTID
        val tidMidFragment = InputListFragment.newInstance(inputList, "Save",
            InputListFragment.ButtonListener{
                merchantId = inputList[0].text
                Log.d("Merchant ID",merchantId.toString())
                terminalId = inputList[1].text
                Log.d("Terminal ID",terminalId.toString())
                activationViewModel.hostIP.observe(mainActivity){//to get the current Val
                    activationViewModel.updateActivation(terminalId, merchantId,it)
                }
                mainActivity.observeTIDMID()
                mainActivity.popFragment()
            })

        mainActivity.addFragment(tidMidFragment as Fragment)

    }
}