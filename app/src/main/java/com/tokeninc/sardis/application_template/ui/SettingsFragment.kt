package com.tokeninc.sardis.application_template.ui

import MenuItem
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.token.uicomponents.CustomInput.CustomInputFormat
import com.token.uicomponents.CustomInput.EditTextInputType
import com.token.uicomponents.CustomInput.InputListFragment
import com.token.uicomponents.CustomInput.InputValidator
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.tokeninc.sardis.application_template.databinding.FragmentSettingsBinding
import com.tokeninc.sardis.application_template.viewmodels.ActivationViewModel
import org.apache.commons.lang3.StringUtils


/**
 * This fragment is for Setting Configuration, it depends on Activation Database
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainActivity: MainActivity
    private lateinit var intent: Intent

    private var isBankActivateAction = true
    var activationViewModel: ActivationViewModel? = null

    private var terminalId: String? = null
    private var merchantId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isBankActivateAction = intent.action != null && intent.action.equals("Activate_Bank")
        if (isBankActivateAction) {
            terminalId = intent.getStringExtra("terminalID")
            merchantId = intent.getStringExtra("merchantID")
            //startActivation()
        } else {
            showMenu()
        }
    }

    /**
     * This is for setting some variables in this class, it is called from mainActivity
     */
    fun setter(mainActivity: MainActivity, activationViewModel: ActivationViewModel, intent: Intent){
        this.intent = intent
        this.mainActivity = mainActivity
        this.activationViewModel = activationViewModel
    }

    /**
     * This prepares menu, when user clicks Setup, Terminal ID & Merchant ID fragment will be opened
     * If user clicks Host Settings s/he can change IP Port configuration with opening page.
     */
    private fun showMenu(){
        val menuItems = mutableListOf<IListMenuItem>()
        menuItems.add(MenuItem("Setup", {
            addTidMidFragment()
        }))
        menuItems.add(MenuItem("Host Settings", {
            addIPFragment()
        }))
        activationViewModel!!.menuItemList = menuItems
        activationViewModel!!.replaceFragment(mainActivity)
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

        inputList[0].text = activationViewModel!!.getHostIP()
        inputList[1].text = activationViewModel!!.getHostPort()
        val hostFragment = InputListFragment.newInstance(inputList, "Save",
            InputListFragment.ButtonListener{
                val ipNo = inputList[0].text
                val portNo = inputList[1].text
                activationViewModel!!.updateConnection(ipNo, portNo)
            })

        mainActivity.addFragment(hostFragment as Fragment)
    }

    /**
     * This is for validating the input.
     */
    private fun validate(customInputFormat: com.token.uicomponents.CustomInput.CustomInputFormat): Boolean {
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

        inputList[0].text = activationViewModel!!.getMerchantId()
        inputList[1].text = activationViewModel!!.getTerminalId()

        val tidMidFragment = InputListFragment.newInstance(inputList, "Save",
            InputListFragment.ButtonListener{
                merchantId = inputList[0].text
                terminalId = inputList[1].text
                activationViewModel!!.updateActivation(terminalId, merchantId)
            })

        mainActivity.addFragment(tidMidFragment as Fragment)

    }
}