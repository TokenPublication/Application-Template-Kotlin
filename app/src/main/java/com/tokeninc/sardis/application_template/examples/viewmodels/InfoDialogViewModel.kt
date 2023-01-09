package com.tokeninc.sardis.application_template.examples.viewmodels

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.examples.ExampleActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InfoDialogViewModel: ViewModel() {

    var list = mutableListOf<IListMenuItem>()

    fun replaceFragment(exampleActivity: ExampleActivity){
        val fragment = ListMenuFragment.newInstance(list, "Info Dialog", false, R.drawable.token_logo)
        viewModelScope.launch(Dispatchers.Main) {
            exampleActivity.replaceFragment(fragment as Fragment)        }
    }


}