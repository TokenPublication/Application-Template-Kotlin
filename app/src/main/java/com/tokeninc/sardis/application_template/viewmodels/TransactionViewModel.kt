package com.tokeninc.sardis.application_template.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.tokeninc.sardis.application_template.database.entities.Transaction
import com.tokeninc.sardis.application_template.repositories.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransactionViewModel(private val transactionRepository: TransactionRepository): ViewModel() {

    var allTransactions: List<Transaction?>? = transactionRepository.allTransactions

    val list = MutableLiveData<List<Transaction?>>()
    var cardNumber: String? = null

    var menuItemList = mutableListOf<IListMenuItem>()

    /**
     * It is for getting transactions with card number from database for recycler view on Void Operations.
     */
    fun createLiveData(): MutableList<Transaction?> {
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