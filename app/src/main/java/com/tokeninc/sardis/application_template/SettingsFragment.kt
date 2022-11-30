package com.tokeninc.sardis.application_template

import MenuItem
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.tokeninc.sardis.application_template.Database.ActivationDB
import com.tokeninc.sardis.application_template.Database.DatabaseHelper
import com.tokeninc.sardis.application_template.databinding.FragmentSettingsBinding
import org.apache.commons.lang3.StringUtils


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val mainActivity = MainActivity()
    private var menuFragment: ListMenuFragment? = null
    private var hostFragment: InputListFragment? = null
    private  var TidMidFragment:InputListFragment? = null
    //this is variables is for hold data that are got from activity.
    var _context: Context? = null
    var resultIntent: Intent? = null
    var databaseHelper: DatabaseHelper? = null
    private var isBankActivateAction = true
    private  var DB_getAllTransactionsCount = true

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
        // _context comes from mainActivity before SettingsFragment calling
        //Log.w("Settings/OnViewCreated","DatabaseHelper çağırdı")
        databaseHelper = ActivationDB(_context!!)
        // resultIntent comes from mainActivity before SettingsFragment calling
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
        menuFragment = ListMenuFragment.newInstance(menuItems,"Settings",
            true, R.drawable.token_logo)
        parentFragmentManager.beginTransaction().apply {
            replace(binding.container.id, menuFragment!!)
            commit()
        }
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

        inputList[0].text = databaseHelper!!.getIP_NO()
        inputList[1].text = databaseHelper!!.getPort()

        hostFragment = InputListFragment.newInstance(inputList, "Save",
            InputListFragment.ButtonListener{
                ip_no = inputList[0].text
                port_no = inputList[1].text
                databaseHelper!!.updateIP_NO(ip_no)
                databaseHelper!!.updatePort(port_no)
                Toast.makeText(_context,"IP: $ip_no  PORT: $port_no",Toast.LENGTH_LONG).show()
            })

        parentFragmentManager.beginTransaction().apply {
            replace(binding.container.id, hostFragment!!)
            addToBackStack("")
            commit()
        }
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

        inputList[0].text = databaseHelper!!.getMerchantId()
        inputList[1].text = databaseHelper!!.getTerminalId()

        TidMidFragment = InputListFragment.newInstance(inputList, "Save",
            InputListFragment.ButtonListener{
                merchantId = inputList[0].text
                terminalId = inputList[1].text
                databaseHelper!!.updateMerchantId(merchantId)
                databaseHelper!!.updateTerminalId(terminalId)
                Toast.makeText(_context,"merc: $merchantId  term: $terminalId",Toast.LENGTH_LONG).show()
            })

        parentFragmentManager.beginTransaction().apply {
            replace(binding.container.id, TidMidFragment!!)
            addToBackStack("")
            commit()
        }

    }



}