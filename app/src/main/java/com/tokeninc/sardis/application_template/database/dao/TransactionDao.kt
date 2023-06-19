package com.tokeninc.sardis.application_template.database.dao

import androidx.room.*
import com.tokeninc.sardis.application_template.database.DatabaseInfo
import com.tokeninc.sardis.application_template.database.entities.Transaction
import com.tokeninc.sardis.application_template.entities.col_names.TransactionCols

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction) //Insertlemeden contenti transactiona çevirmek gerek..

    @Query("SELECT * FROM ${DatabaseInfo.TRANSACTIONTABLE} WHERE ${TransactionCols.Col_PAN} = :cardNo AND ${TransactionCols.Col_IsVoid} <> 1 ORDER BY  ${TransactionCols.Col_GUP_SN} DESC")
    fun getTransactionsByCardNo(cardNo: String): List<Transaction?>?

    @Query("SELECT * FROM ${DatabaseInfo.TRANSACTIONTABLE} WHERE ${TransactionCols.Col_HostLogKey} = :refNo")
    fun getTransactionsByRefNo(refNo: String): List<Transaction?>?

    //To implement getter without using livedata (when using function in IO thread), it needs to be in IO thread.
    @Query("SELECT * FROM ${DatabaseInfo.TRANSACTIONTABLE} ORDER BY ${TransactionCols.Col_GUP_SN}")
    suspend fun getAllTransactions(): List<Transaction?>?

    @Query("UPDATE ${DatabaseInfo.TRANSACTIONTABLE} SET ${TransactionCols.Col_IsVoid} = 1, ${TransactionCols.Col_VoidDateTime} = :date, ${TransactionCols.Col_SID} = :card_SID WHERE ${TransactionCols.Col_GUP_SN} = :gupSN")
    suspend fun setVoid(gupSN: Int, date: String?, card_SID: String?)

    @Query("DELETE FROM ${DatabaseInfo.TRANSACTIONTABLE}")
    suspend fun deleteAll()
}