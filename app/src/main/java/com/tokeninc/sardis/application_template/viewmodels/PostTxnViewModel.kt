package com.tokeninc.sardis.application_template.viewmodels
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.tokeninc.sardis.application_template.ui.MainActivity
import com.tokeninc.sardis.application_template.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * This class doesn't contains actual viewModel properties, it's only for use coroutine's viewModelScope in
 * displaying data in UI.
 */
class PostTxnViewModel: ViewModel() {

    var list = mutableListOf<IListMenuItem>()

    fun replaceFragment(mainActivity: MainActivity){
        val menuFragment = ListMenuFragment.newInstance(list,"PostTxn",
            true, R.drawable.token_logo)
        viewModelScope.launch(Dispatchers.Main) {
            mainActivity.replaceFragment(menuFragment as Fragment)
        }
    }

}
