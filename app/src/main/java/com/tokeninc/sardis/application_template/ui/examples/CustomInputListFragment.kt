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
class CustomInputListFragment(private val exampleFragment: ExampleFragment) : Fragment(R.layout.fragment_custom_input_list) {

    private val validator = InputValidator { input -> input.text.length == 19 }
    private val validator2 = InputValidator { input -> input.text.length == 10 }
    private val inputList = mutableListOf<CustomInputFormat>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareData()
        val fragment = InputListFragment.newInstance(inputList)
        exampleFragment.mainActivity.replaceFragment(fragment as Fragment)
    }

    private fun prepareData(){
        val customInputFormat = CustomInputFormat("Text", EditTextInputType.Text, 8, null, null)
        customInputFormat.text = "00000016"
        inputList.add(customInputFormat)
        inputList.add(CustomInputFormat(exampleFragment.getStrings(R.string.card_number), EditTextInputType.CreditCardNumber,null, exampleFragment.getStrings(R.string.invalid_card_number), validator))
        inputList.add(CustomInputFormat(exampleFragment.getStrings(R.string.expire_date), EditTextInputType.ExpiryDate,null, null, null))
        inputList.add(CustomInputFormat(exampleFragment.getStrings(R.string.cvv), EditTextInputType.CVV, null,null, null))
        inputList.add(CustomInputFormat(exampleFragment.getStrings(R.string.date), EditTextInputType.Date, null,null, null))
        inputList.add(CustomInputFormat(exampleFragment.getStrings(R.string.time), EditTextInputType.Time, null,null, null))
        inputList.add(CustomInputFormat(exampleFragment.getStrings(R.string.number), EditTextInputType.Number, null,null, null))
        inputList.add(CustomInputFormat(exampleFragment.getStrings(R.string.amount), EditTextInputType.Amount, null,null, null))
        inputList.add(CustomInputFormat(exampleFragment.getStrings(R.string.ip), EditTextInputType.IpAddress, null,null, null))
        inputList.add(CustomInputFormat(exampleFragment.getStrings(R.string.password), EditTextInputType.Password, null,null, null))
        inputList.add(CustomInputFormat(exampleFragment.getStrings(R.string.num_password), EditTextInputType.NumberPassword, null, null, null))
        inputList.add(CustomInputFormat(exampleFragment.getStrings(R.string.new_text), EditTextInputType.Text, null, exampleFragment.getStrings(R.string.invalid_text), validator2))
        inputList[1].text = "1234"
    }
}
