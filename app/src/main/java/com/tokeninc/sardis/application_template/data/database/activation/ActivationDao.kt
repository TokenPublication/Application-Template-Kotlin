package com.tokeninc.sardis.application_template.data.database.activation

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tokeninc.sardis.application_template.data.database.DatabaseInfo

@Dao
interface ActivationDao {

    @Insert
    suspend fun insertActivation(activation: Activation)
    @Query("UPDATE ${DatabaseInfo.ACTIVATION_TABLE} SET ${ActivationCols.ColIPNo} = :ip, ${ActivationCols.ColPortNo} = :port ")
    suspend fun updateConnection(ip: String?, port: String?)

    @Query("UPDATE ${DatabaseInfo.ACTIVATION_TABLE} SET ${ActivationCols.ColTerminalId} = :terminalId, ${ActivationCols.ColMerchantId} = :merchantId")
    suspend fun updateActivation(terminalId: String?, merchantId: String?)

    @Query("SELECT ${ActivationCols.ColMerchantId} FROM ${DatabaseInfo.ACTIVATION_TABLE} LIMIT 1")
    fun getMerchantId(): String?

    @Query("SELECT ${ActivationCols.ColTerminalId} FROM ${DatabaseInfo.ACTIVATION_TABLE} LIMIT 1")
    fun getTerminalId(): String?

    @Query("SELECT ${ActivationCols.ColIPNo} FROM ${DatabaseInfo.ACTIVATION_TABLE} LIMIT 1")
    fun getHostIP(): String

    @Query("SELECT ${ActivationCols.ColPortNo} FROM ${DatabaseInfo.ACTIVATION_TABLE} LIMIT 1")
    fun getHostPort(): String
}
