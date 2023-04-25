package com.tokeninc.sardis.application_template.database.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tokeninc.sardis.application_template.database.DatabaseInfo

@Dao
interface BatchDao {

    @Query("UPDATE ${DatabaseInfo.BATCHTABLE} SET ${BatchCols.col_ulGUP_SN} = :groupSn + 1 WHERE ${BatchCols.col_ulGUP_SN} = :groupSn")
    suspend fun updateGUPSN(groupSn: Int) //PK değil diye hata olur mu ?

    @Query("UPDATE ${DatabaseInfo.BATCHTABLE} SET ${BatchCols.col_ulGUP_SN} = 1, ${BatchCols.col_batchNo} = :batchNo + 1 WHERE ${BatchCols.col_batchNo} = :batchNo")
    suspend fun updateBatchNo(batchNo: Int)

    @Query("SELECT ${BatchCols.col_ulGUP_SN} FROM ${DatabaseInfo.BATCHTABLE} LIMIT 1")
    fun getGUPSN(): Int

    @Query("SELECT ${BatchCols.col_batchNo} FROM ${DatabaseInfo.BATCHTABLE} LIMIT 1")
    fun getBatchNo(): Int

    @Query("DELETE FROM ${DatabaseInfo.BATCHTABLE}")
    fun deleteAll()

}