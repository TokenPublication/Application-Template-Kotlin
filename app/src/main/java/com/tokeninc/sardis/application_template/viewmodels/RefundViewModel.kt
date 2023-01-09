package com.tokeninc.sardis.application_template.viewmodels

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.RefundFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class RefundViewModel: ViewModel() {

    var list = mutableListOf<IListMenuItem>()

    fun replaceFragment(mainActivity: MainActivity){
        val menuFragment = ListMenuFragment.newInstance(list,"Refund",
            true, R.drawable.token_logo)
        mainActivity.replaceFragment(menuFragment as Fragment)
    }
}