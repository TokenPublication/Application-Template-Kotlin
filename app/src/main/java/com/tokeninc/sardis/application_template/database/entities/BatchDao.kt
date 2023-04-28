package com.tokeninc.sardis.application_template.database.entities

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tokeninc.sardis.application_template.database.DatabaseInfo

@Dao
interface BatchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun initBatch(batch: Batch)

    @Query("UPDATE ${DatabaseInfo.BATCHTABLE} SET ${BatchCols.col_ulGUP_SN} = :groupSn + 1 WHERE ${BatchCols.col_ulGUP_SN} = :groupSn")
    suspend fun updateGUPSN(groupSn: Int) //PK değil diye hata olur mu ?

    @Query("UPDATE ${DatabaseInfo.BATCHTABLE} SET ${BatchCols.col_ulGUP_SN} = 1, ${BatchCols.col_batchNo} = :batchNo + 1 WHERE ${BatchCols.col_batchNo} = :batchNo")
    suspend fun updateBatchNo(batchNo: Int)

    @Query("UPDATE ${DatabaseInfo.BATCHTABLE} SET ${BatchCols.col_previous_batch_slip} = :batchSlip WHERE ${BatchCols.col_batchNo} = :batchNo")
    suspend fun updateBatchSlip(batchSlip: String?,batchNo: Int)

    @Query("SELECT ${BatchCols.col_ulGUP_SN} FROM ${DatabaseInfo.BATCHTABLE} LIMIT 1")
    fun getGUPSN(): LiveData<Int>

    @Query("SELECT ${BatchCols.col_batchNo} FROM ${DatabaseInfo.BATCHTABLE} LIMIT 1")
    fun getBatchNo(): LiveData<Int>

    @Query("SELECT ${BatchCols.col_previous_batch_slip} FROM ${DatabaseInfo.BATCHTABLE} LIMIT 1")
    fun getBatchPreviousSlip(): LiveData<String?>

    @Query("DELETE FROM ${DatabaseInfo.BATCHTABLE}")
    fun deleteAll()

}