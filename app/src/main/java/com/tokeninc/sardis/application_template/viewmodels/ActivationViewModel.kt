package com.tokeninc.sardis.application_template.viewmodels

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.tokeninc.sardis.application_template.ui.MainActivity
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.database.activation.ActivationDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActivationViewModel(val database: ActivationDB):ViewModel() {

    var menuItemList = mutableListOf<IListMenuItem>()

    fun replaceFragment(mainActivity: MainActivity){
        val menuFragment = ListMenuFragment.newInstance(menuItemList,"Settings",
            true, R.drawable.token_logo)
        viewModelScope.launch(Dispatchers.Main) {
            mainActivity.replaceFragment(menuFragment as Fragment)
        }
    }

    fun insertConnection(IP: String?, port: String?): Boolean {
        return database.insertConnection(IP, port)
    }

    fun updateConnection(IP: String?, port: String?) {
        database.updateConnection(IP, port)
    }

    fun updateActivation(terminalId: String?, merchantId: String?) {
        database.updateActivation(terminalId,merchantId)
    }

    fun getMerchantId(): String?{
        return database.getMerchantId()
    }

    fun getTerminalId(): String? {
        return database.getTerminalId()
    }

    fun getHostIP(): String? {
        return database.getHostIP()
    }

    fun getHostPort(): String? {
        return database.getHostPort()
    }

}