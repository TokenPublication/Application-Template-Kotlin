package com.tokeninc.sardis.application_template.database.entities

import android.content.ContentValues
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransactionViewModel(private val transactionRepository: TransactionRepository): ViewModel() {

    var allTransactions: LiveData<List<TransactionTable?>> = transactionRepository.allTransactions

    fun getTransactionsByRefNo(refNo: String): LiveData<List<TransactionTable?>>{
        return transactionRepository.getTransactionsByRefNo(refNo)
    }

    fun getTransactionsByCardNo(cardNo: String): LiveData<List<TransactionTable?>>{
        return transactionRepository.getTransactionsByCardNo(cardNo)
    }

    fun insertTransaction(transaction: TransactionTable){
        viewModelScope.launch(Dispatchers.IO){
            transactionRepository.insertTransaction(transaction)
        }
    }

    fun setVoid(gupSN: Int, date: String?, card_SID: String){
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