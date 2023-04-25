package com.tokeninc.sardis.application_template.database.entities

import android.content.ContentValues
import androidx.lifecycle.LiveData
import androidx.room.*
import com.tokeninc.sardis.application_template.database.DatabaseInfo
import com.tokeninc.sardis.application_template.database.transaction.TransactionCol
import com.tokeninc.sardis.application_template.entities.ICCCard

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionTable) //Insertlemeden contenti transactiona çevirmek gerek..

    @Query("SELECT * FROM ${DatabaseInfo.TRANSACTIONTABLE} WHERE ${TransactionCols.Col_PAN} = :cardNo AND ${TransactionCols.Col_IsVoid} <> 1 ORDER BY  ${TransactionCols.Col_GUP_SN} DESC")
    fun getTransactionsByCardNo(cardNo: String): LiveData<List<TransactionTable?>>

    @Query("SELECT * FROM ${DatabaseInfo.TRANSACTIONTABLE} WHERE ${TransactionCols.Col_HostLogKey} = :refNo")
    fun getTransactionsByRefNo(refNo: String): LiveData<List<TransactionTable?>>

    @Query("SELECT * FROM ${DatabaseInfo.TRANSACTIONTABLE} ORDER BY ${TransactionCols.Col_GUP_SN}")
    fun getAllTransactions(): LiveData<List<TransactionTable?>>

    @Query("UPDATE ${DatabaseInfo.TRANSACTIONTABLE} SET ${TransactionCols.Col_IsVoid} = 1, ${TransactionCols.Col_VoidDateTime} = :date, ${TransactionCols.Col_SID} = :card_SID WHERE ${TransactionCols.Col_GUP_SN} = :gupSN")
    suspend fun setVoid(gupSN: Int, date: String?, card_SID: String)

    @Query("DELETE FROM ${DatabaseInfo.TRANSACTIONTABLE}")
    suspend fun deleteAll()
}