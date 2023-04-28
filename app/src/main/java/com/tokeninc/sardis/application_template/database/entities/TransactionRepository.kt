package com.tokeninc.sardis.application_template.database.entities

import androidx.lifecycle.LiveData

class TransactionRepository(private val transactionDao: TransactionDao) {

    var allTransactions: LiveData<List<Transaction?>> = transactionDao.getAllTransactions()

    fun getTransactionsByRefNo(refNo: String): LiveData<List<Transaction?>>{
        return transactionDao.getTransactionsByRefNo(refNo)
    }

    fun getTransactionsByCardNo(cardNo: String): LiveData<List<Transaction?>>{
        return transactionDao.getTransactionsByCardNo(cardNo)
    }

    suspend fun insertTransaction(transaction: Transaction){
        transactionDao.insertTransaction(transaction)
    }

    suspend fun setVoid(gupSN: Int, date: String?, card_SID: String?){
        transactionDao.setVoid(gupSN,date,card_SID)
    }

    suspend fun deleteAll(){
        transactionDao.deleteAll()
    }

}