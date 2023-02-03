package com.tokeninc.sardis.application_template.viewmodels

import android.content.ContentValues
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.tokeninc.sardis.application_template.database.transaction.TransactionDB
import com.tokeninc.sardis.application_template.entities.ICCCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

/** This connects view with Models
 * It is the interlayer between UI and Transaction Table
 */
class TransactionViewModel(val database: TransactionDB): ViewModel() {

    val list = MutableLiveData<List<ContentValues?>>()
    var cardNumber: String? = null

    var menuItemList = mutableListOf<IListMenuItem>()


    fun insertTransaction(contentValues: ContentValues?): Boolean {
        return database.insertTransaction(contentValues)
    }

    fun deleteAll(){
        database.deleteAll()
    }

    fun getTransactionsByCardNo(cardNo: String): List<ContentValues?> {
        return database.getTransactionsByCardNo(cardNo)
    }

    fun getAllTransactions(): List<ContentValues?> {
        return database.getAllTransactions()
    }

    /**
     * It is for getting transactions with card number from database for recycler view on Void Operations.
     */
    fun createLiveData(): MutableList<ContentValues?> {
        list.value = getTransactionsByCardNo(cardNumber!!)
        return list.value!!.toMutableList()
    }

    fun setVoid(gupSN: Int, date: String?, card: ICCCard): Boolean {
        return database.setVoid(gupSN, date, card) == 1
    }

}