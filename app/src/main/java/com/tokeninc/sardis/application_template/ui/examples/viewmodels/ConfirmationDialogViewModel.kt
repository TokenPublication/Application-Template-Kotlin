package com.tokeninc.sardis.application_template.ui.examples.viewmodels

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.ui.examples.ExampleActivity

class ConfirmationDialogViewModel: ViewModel() {


    var list = mutableListOf<IListMenuItem>()

    fun replaceFragment(exampleActivity: ExampleActivity){
        val fragment = ListMenuFragment.newInstance(list, "Confirmation", false, R.drawable.token_logo)
        exampleActivity.replaceFragment(fragment as Fragment)
    }

}