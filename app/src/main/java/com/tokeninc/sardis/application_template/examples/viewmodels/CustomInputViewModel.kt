package com.tokeninc.sardis.application_template.examples.viewmodels

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.token.uicomponents.CustomInput.CustomInputFormat
import com.token.uicomponents.CustomInput.InputListFragment
import com.tokeninc.sardis.application_template.examples.ExampleActivity

class CustomInputViewModel:ViewModel() {

    var list = mutableListOf<CustomInputFormat>()

    fun replaceFragment(exampleActivity: ExampleActivity){
        val fragment = InputListFragment.newInstance(list)
        exampleActivity.replaceFragment(fragment as Fragment)
    }

}