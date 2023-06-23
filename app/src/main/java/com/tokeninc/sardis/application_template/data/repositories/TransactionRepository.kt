package com.tokeninc.sardis.application_template.data.repositories

import com.tokeninc.sardis.application_template.data.database.transaction.TransactionDao
import com.tokeninc.sardis.application_template.data.database.transaction.Transaction
import javax.inject.Inject

class TransactionRepository @Inject constructor(private val transactionDao: TransactionDao) {

    suspend fun allTransactions(): List<Transaction?>? = transactionDao.getAllTransactions()

    fun getTransactionsByRefNo(refNo: String): List<Transaction?>?{
        return transactionDao.getTransactionsByRefNo(refNo)
    }

    fun getTransactionsByCardNo(cardNo: String): List<Transaction?>?{
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