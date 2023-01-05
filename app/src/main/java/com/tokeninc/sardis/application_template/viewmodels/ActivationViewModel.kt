package com.tokeninc.sardis.application_template.viewmodels

import androidx.lifecycle.ViewModel
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.tokeninc.sardis.application_template.database.activation.ActivationDB

class ActivationViewModel(val database: ActivationDB):ViewModel() {

    var menuItemList = mutableListOf<IListMenuItem>()

    fun insertConnection(IP: String?, port: String?): Boolean {
        return database.insertConnection(IP, port)
    }

    fun insertActivation(terminalId: String?, merchantId: String?) {
        database.insertActivation(terminalId,merchantId)
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