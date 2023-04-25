package com.tokeninc.sardis.application_template.database.entities

import android.content.ContentValues
import androidx.lifecycle.LiveData

class TransactionRepository(private val transactionDao: TransactionDao) {

    var allTransactions: LiveData<List<TransactionTable?>> = transactionDao.getAllTransactions()

    fun getTransactionsByRefNo(refNo: String): LiveData<List<TransactionTable?>>{
        return transactionDao.getTransactionsByRefNo(refNo)
    }

    fun getTransactionsByCardNo(cardNo: String): LiveData<List<TransactionTable?>>{
        return transactionDao.getTransactionsByCardNo(cardNo)
    }

    suspend fun insertTransaction(transaction: TransactionTable){
        transactionDao.insertTransaction(transaction)
    }

    suspend fun setVoid(gupSN: Int, date: String?, card_SID: String){
        transactionDao.setVoid(gupSN,date,card_SID)
    }

    suspend fun deleteAll(){
        transactionDao.deleteAll()
    }

}