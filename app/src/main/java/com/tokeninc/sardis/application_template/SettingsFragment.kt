package com.tokeninc.sardis.application_template

import MenuItem
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.token.uicomponents.CustomInput.EditTextInputType
import com.token.uicomponents.CustomInput.InputValidator
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.tokeninc.sardis.application_template.databinding.FragmentSettingsBinding
import org.apache.commons.lang3.StringUtils


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val mainActivity = MainActivity()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater,container,false)
        return binding.root
    }
/*

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

    }

    private fun addIPFragment(){

        var inputList = mutableListOf<CustomInputFormat>()
        inputList.add(CustomInputFormat("IP",EditTextInputType.IpAddress,
        null, "Invalid IP!", InputValidator {

            }))

    }

    fun validate(customInputFormat: com.token.uicomponents.CustomInput.CustomInputFormat): Boolean {
        val text = customInputFormat.text
        var isValid =
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
     */


}