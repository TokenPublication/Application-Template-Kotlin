package com.tokeninc.sardis.application_template

import MenuItem
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
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.tokeninc.sardis.application_template.databinding.FragmentSettingsBinding
import org.apache.commons.lang3.StringUtils


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val mainActivity = MainActivity()
    private var menuFragment: ListMenuFragment? = null
    private var hostFragment: InputListFragment? = null
    private  var TidMidFragment:InputListFragment? = null

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
        showMenu()
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

        hostFragment = InputListFragment.newInstance(inputList)
        parentFragmentManager.beginTransaction().apply {
            replace(binding.container.id, hostFragment!!)
            addToBackStack("")
            commit()
        }
    }

    private fun validate(customInputFormat: com.token.uicomponents.CustomInput.CustomInputFormat): Boolean {
        val text = customInputFormat.text
        //regex değil delimiter geldiği için toTypedArray yapmış doğru mu?
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
        TidMidFragment = InputListFragment.newInstance(inputList)
        parentFragmentManager.beginTransaction().apply {
            replace(binding.container.id, TidMidFragment!!)
            addToBackStack("")
            commit()
        }

    }



}