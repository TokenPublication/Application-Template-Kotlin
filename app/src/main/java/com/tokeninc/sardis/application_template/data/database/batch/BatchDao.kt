package com.tokeninc.sardis.application_template.data.database.batch

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tokeninc.sardis.application_template.data.database.DatabaseInfo


@Dao
interface BatchDao {
    @Insert
    suspend fun insertBatch(batch: Batch)

    @Query("UPDATE ${DatabaseInfo.BATCH_TABLE} SET ${BatchCols.col_ulSTN} = CASE WHEN ${BatchCols.col_ulSTN} >= 999 THEN 0 ELSE ${BatchCols.col_ulSTN} + 1 END WHERE ROWID = (SELECT rowID FROM ${DatabaseInfo.BATCH_TABLE} LIMIT 1)")
    fun updateSTN()

    @Query("UPDATE ${DatabaseInfo.BATCH_TABLE} SET ${BatchCols.col_ulGUP_SN} = :groupSn + 1 WHERE ${BatchCols.col_ulGUP_SN} = :groupSn")
    suspend fun updateGUPSN(groupSn: Int)

    @Query("UPDATE ${DatabaseInfo.BATCH_TABLE} SET ${BatchCols.col_ulGUP_SN} = 1, ${BatchCols.col_batchNo} = :batchNo + 1 WHERE ${BatchCols.col_batchNo} = :batchNo")
    suspend fun updateBatchNo(batchNo: Int)

    @Query("UPDATE ${DatabaseInfo.BATCH_TABLE} SET ${BatchCols.col_previous_batch_slip} = :batchSlip WHERE ${BatchCols.col_batchNo} = :batchNo")
    suspend fun updateBatchSlip(batchSlip: String?,batchNo: Int?)
    @Query("SELECT ${BatchCols.col_ulGUP_SN} FROM ${DatabaseInfo.BATCH_TABLE} LIMIT 1")
    fun getGUPSN(): Int

    @Query("SELECT ${BatchCols.col_batchNo} FROM ${DatabaseInfo.BATCH_TABLE} LIMIT 1")
    fun getBatchNo(): Int

    @Query("SELECT ${BatchCols.col_ulSTN} FROM ${DatabaseInfo.BATCH_TABLE} LIMIT 1")
    fun getSTN(): Int

    @Query("SELECT ${BatchCols.col_previous_batch_slip} FROM ${DatabaseInfo.BATCH_TABLE} LIMIT 1")
    fun getBatchPreviousSlip(): LiveData<String?>

}
