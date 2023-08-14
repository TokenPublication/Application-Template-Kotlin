package com.tokeninc.sardis.application_template.ui.examples

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.token.uicomponents.CustomInput.CustomInputFormat
import com.token.uicomponents.CustomInput.EditTextInputType
import com.token.uicomponents.CustomInput.InputListFragment
import com.token.uicomponents.CustomInput.InputValidator
import com.tokeninc.sardis.application_template.R

/** It can be deleted
 * This fragment includes Custom Input List methods for example activity
 */
class CustomInputListFragment(private val exampleActivity: ExampleActivity) : Fragment(R.layout.fragment_custom_input_list) {

    var validator = InputValidator { input -> input.text.length == 19 }
    var validator2 = InputValidator { input -> input.text.length == 10 }
    var inputList = mutableListOf<CustomInputFormat>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareData()
        val fragment = InputListFragment.newInstance(inputList)
        exampleActivity.replaceFragment(fragment as Fragment)
    }

    private fun prepareData(){
        val customInputFormat = CustomInputFormat("Text", EditTextInputType.Text, 8, null, null)
        customInputFormat.text = "00000016"
        inputList.add(customInputFormat)
        inputList.add(CustomInputFormat("Card Number", EditTextInputType.CreditCardNumber,null, "Invalid card number!", validator))
        inputList.add(CustomInputFormat("Expire Date", EditTextInputType.ExpiryDate,null, null, null))
        inputList.add(CustomInputFormat("CVV", EditTextInputType.CVV, null,null, null))
        inputList.add(CustomInputFormat("Date", EditTextInputType.Date, null,null, null))
        inputList.add(CustomInputFormat("Time", EditTextInputType.Time, null,null, null))
        inputList.add(CustomInputFormat("Number", EditTextInputType.Number, null,null, null))
        inputList.add(CustomInputFormat("Amount", EditTextInputType.Amount, null,null, null))
        inputList.add(CustomInputFormat("IP", EditTextInputType.IpAddress, null,null, null))
        inputList.add(CustomInputFormat("Password", EditTextInputType.Password, null,null, null))
        inputList.add(CustomInputFormat("Password (Num)", EditTextInputType.NumberPassword, null, null, null))
        inputList.add(CustomInputFormat("New Text", EditTextInputType.Text, null, "Max text size 10", validator2))
        inputList[1].text = "1234"
    }
}
