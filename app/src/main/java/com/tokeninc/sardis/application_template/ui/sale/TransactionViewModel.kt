package com.tokeninc.sardis.application_template.ui.sale

import android.content.ContentValues
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.infodialog.InfoDialog
import com.tokeninc.sardis.application_template.data.database.transaction.Transaction
import com.tokeninc.sardis.application_template.data.entities.card_entities.ICCCard
import com.tokeninc.sardis.application_template.data.entities.responses.TransactionResponse
import com.tokeninc.sardis.application_template.data.repositories.TransactionRepository
import com.tokeninc.sardis.application_template.ui.posttxn.batch.BatchViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(private val transactionRepository: TransactionRepository): ViewModel() {

    fun allTransactions(): List<Transaction?>? {
        var returnList: List<Transaction?>?
        runBlocking {
            val deferred = viewModelScope.async(Dispatchers.IO){
                transactionRepository.allTransactions()
            }
            returnList = deferred.await()
        }
        return returnList
    }

    val list = MutableLiveData<List<Transaction?>>()
    var menuItemList = mutableListOf<IListMenuItem>()

    /**
     * It is for getting transactions with card number from database for recycler view on Void Operations.
     */
    fun createLiveData(cardNumber: String?): MutableList<Transaction?> {
        list.value = getTransactionsByCardNo(cardNumber!!)!!
        return list.value!!.toMutableList()
    }

    fun getTransactionsByRefNo(refNo: String): List<Transaction?>?{
        return transactionRepository.getTransactionsByRefNo(refNo)
    }

    fun getTransactionsByCardNo(cardNo: String): List<Transaction?>?{
        return transactionRepository.getTransactionsByCardNo(cardNo)
    }

    fun insertTransaction(transaction: Transaction){
        viewModelScope.launch(Dispatchers.IO){
            transactionRepository.insertTransaction(transaction)
        }
    }

    fun setVoid(gupSN: Int, date: String?, card_SID: String?){
        viewModelScope.launch(Dispatchers.IO) {
            transactionRepository.setVoid(gupSN, date, card_SID)
        }
    }

    fun deleteAll(){
        viewModelScope.launch(Dispatchers.IO) {
            transactionRepository.deleteAll()
        }
    }

}

