package com.tokeninc.sardis.application_template.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tokeninc.sardis.application_template.database.DatabaseInfo
import com.tokeninc.sardis.application_template.database.entities.Batch
import com.tokeninc.sardis.application_template.entities.col_names.BatchCols

@Dao
interface BatchDao {

    @Insert
    suspend fun initBatch(batch: Batch)

    @Query("UPDATE ${DatabaseInfo.BATCHTABLE} SET ${BatchCols.col_ulGUP_SN} = :groupSn + 1 WHERE ${BatchCols.col_ulGUP_SN} = :groupSn")
    suspend fun updateGUPSN(groupSn: Int) //PK değil diye hata olur mu ?
    //gupSN 0 olmuş ilk işlemde ? eğer hep bir geriden geliyorsa 1 den başlat

    @Query("UPDATE ${DatabaseInfo.BATCHTABLE} SET ${BatchCols.col_ulGUP_SN} = 1, ${BatchCols.col_batchNo} = :batchNo + 1 WHERE ${BatchCols.col_batchNo} = :batchNo")
    suspend fun updateBatchNo(batchNo: Int)

    @Query("UPDATE ${DatabaseInfo.BATCHTABLE} SET ${BatchCols.col_previous_batch_slip} = :batchSlip WHERE ${BatchCols.col_batchNo} = :batchNo")
    suspend fun updateBatchSlip(batchSlip: String?,batchNo: Int?)

    @Query("SELECT * FROM ${DatabaseInfo.BATCHTABLE}")
    fun getAllBatch(): List<Batch?>
    @Query("SELECT ${BatchCols.col_ulGUP_SN} FROM ${DatabaseInfo.BATCHTABLE} LIMIT 1")
    fun getGUPSN(): Int //livedata gereksiz UIla işi yoksa

    @Query("SELECT ${BatchCols.col_batchNo} FROM ${DatabaseInfo.BATCHTABLE} LIMIT 1")
    fun getBatchNo(): Int

    @Query("SELECT ${BatchCols.col_previous_batch_slip} FROM ${DatabaseInfo.BATCHTABLE} LIMIT 1")
    fun getBatchPreviousSlip(): String?

    @Query("DELETE FROM ${DatabaseInfo.BATCHTABLE}")
    fun deleteAll()

}