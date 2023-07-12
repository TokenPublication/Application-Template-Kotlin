package com.tokeninc.sardis.application_template.ui.postTxn.refund

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.R

/**
 * This class doesn't contains actual viewModel properties, it's only for use coroutine's viewModelScope in
 * displaying data in UI.
 */
class RefundViewModel: ViewModel() {

    var list = mutableListOf<IListMenuItem>()

    fun replaceFragment(mainActivity: MainActivity){
        val menuFragment = ListMenuFragment.newInstance(list,"Refund",
            true, R.drawable.token_logo)
        mainActivity.replaceFragment(menuFragment as Fragment)
    }
}
