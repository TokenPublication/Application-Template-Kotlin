package com.tokeninc.sardis.application_template.viewmodel

import android.content.ContentValues
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tokeninc.sardis.application_template.database.transaction.TransactionDB

class TransactionViewModel(val cardNumber: String?): ViewModel() {

    var transactionDB: TransactionDB? = null
    val list = MutableLiveData<List<ContentValues?>>()


    //createLiveData() yı çağırman gerek!! bunu işlemlere basarken çağırman lazım onun listenerında
    //sıkıntı olabilir contentvalue olan mutable list ama content valueları neye göre ayırıyor??
    //yok contentVal ler farklı olacak her content valuenun kendine has olacak dataları.
    //content value zaten args olarak parametre alabiliyor, cezbedici yanı da o.
    fun createLiveData(): MutableList<ContentValues?> {
        list.value = transactionDB!!.getTransactionsByCardNo(cardNumber!!)
        return list.value!!.toMutableList()
    }


}