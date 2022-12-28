package com.tokeninc.sardis.application_template.viewmodel

import android.content.ContentValues
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tokeninc.sardis.application_template.database.transaction.TransactionDB

class TransactionViewModel(val cardNumber: String?): ViewModel() {

    var transactionDB: TransactionDB? = null
    val list = MutableLiveData<List<ContentValues?>>()

    fun createLiveData(): MutableList<ContentValues?> {
        list.value = transactionDB!!.getTransactionsByCardNo(cardNumber!!)
        return list.value!!.toMutableList()
    }


}