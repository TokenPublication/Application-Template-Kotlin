package com.tokeninc.sardis.application_template

import MenuItem
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.token.uicomponents.CustomInput.CustomInputFormat
import com.token.uicomponents.CustomInput.EditTextInputType
import com.token.uicomponents.CustomInput.InputListFragment
import com.token.uicomponents.CustomInput.InputValidator
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.tokeninc.sardis.application_template.databinding.FragmentSettingsBinding
import com.tokeninc.sardis.application_template.viewmodels.ActivationViewModel
import org.apache.commons.lang3.StringUtils


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    var mainActivity: MainActivity? = null
    private var menuFragment: ListMenuFragment? = null
    private var hostFragment: InputListFragment? = null
    private  var TidMidFragment:InputListFragment? = null
    //this is variables is for hold data that are got from activity.
    var _context: Context? = null
    var resultIntent: Intent? = null
    private var isBankActivateAction = true
    var activationViewModel: ActivationViewModel? = null

    private var terminalId: String? = null
    private var merchantId: String? = null
    private  var ip_no:kotlin.String? = null
    private  var port_no:kotlin.String? = null

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
        isBankActivateAction = resultIntent != null && resultIntent!!.getAction() != null
                && resultIntent!!.getAction() .equals("Activate_Bank")
        if (isBankActivateAction) {
            terminalId = resultIntent!!.getStringExtra("terminalID")
            merchantId = resultIntent!!.getStringExtra("merchantID")
            //startActivation()
        } else {
            showMenu()
        }

    }


    private fun showMenu(){
        var menuItems = mutableListOf<IListMenuItem>()
        menuItems.add(MenuItem("Setup", {
            addTidMidFragment()
        }))
        menuItems.add(MenuItem("Host Settings", {
            addIPFragment()
        }))
        activationViewModel!!.menuItemList = menuItems
        activationViewModel!!.replaceFragment(mainActivity!!)
    }

    private fun addIPFragment(){

        var inputList = mutableListOf<CustomInputFormat>()
        inputList.add(CustomInputFormat("IP",EditTextInputType.IpAddress,
        null, "Invalid IP!", InputValidator {
                validate(it)
            }))

        inputList.add(CustomInputFormat(
            "Port", EditTextInputType.Number, 4, "Invalid Port!"
        ) { customInputFormat -> customInputFormat.text.length >= 2 && customInputFormat.text.toInt() > 0 })

        inputList[0].text = activationViewModel!!.getHostIP()
        inputList[1].text = activationViewModel!!.getHostPort()
        if (inputList[0].text != null && inputList[1].text!= null)
            Toast.makeText(_context,"HostIP: ${inputList[0].text}, HostPort: ${inputList[1].text }"
            ,Toast.LENGTH_SHORT).show()

        hostFragment = InputListFragment.newInstance(inputList, "Save",
            InputListFragment.ButtonListener{
                ip_no = inputList[0].text
                port_no = inputList[1].text
                if (activationViewModel!!.insertConnection(ip_no, port_no) )
                    Toast.makeText(_context,"IP: $ip_no  PORT: $port_no",Toast.LENGTH_LONG).show()
            })

        mainActivity!!.addFragment(hostFragment as Fragment)
    }

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

    private fun addTidMidFragment() {
        var inputList = mutableListOf<CustomInputFormat>()
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

        TidMidFragment = InputListFragment.newInstance(inputList, "Save",
            InputListFragment.ButtonListener{
                merchantId = inputList[0].text
                terminalId = inputList[1].text
                activationViewModel!!.insertActivation(terminalId, merchantId)
                Toast.makeText(_context,"merc: ${activationViewModel!!.getMerchantId()}  term: ${activationViewModel!!.getTerminalId()}",Toast.LENGTH_LONG).show()
                //mainActivity!!.finish()
            })

        mainActivity!!.addFragment(TidMidFragment as Fragment)

    }
}