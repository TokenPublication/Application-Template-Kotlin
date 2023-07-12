package com.tokeninc.sardis.application_template.data.database.activation

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tokeninc.sardis.application_template.data.database.DatabaseInfo

@Dao
interface ActivationDao {

    @Insert
    suspend fun initActivation(activation: Activation)
    @Query("UPDATE ${DatabaseInfo.ACTIVATION_TABLE} SET ${ActivationCols.ColIPNo} = :ip, ${ActivationCols.ColPortNo} = :port WHERE ${ActivationCols.ColIPNo} = :old_ip")
    suspend fun updateConnection(ip: String?, port: String?, old_ip: String?) //users should get old_ip with getHostPort method

    @Query("UPDATE ${DatabaseInfo.ACTIVATION_TABLE} SET ${ActivationCols.ColTerminalId} = :terminalId, ${ActivationCols.ColMerchantId} = :merchantId WHERE ${ActivationCols.ColIPNo} = :ip")
    suspend fun updateActivation(terminalId: String?, merchantId: String?, ip: String?)

    //To see the current changes in UI you need to use observe method!
    @Query("SELECT ${ActivationCols.ColMerchantId} FROM ${DatabaseInfo.ACTIVATION_TABLE} LIMIT 1")
    fun getMerchantId(): LiveData<String?> //Because it is showed in UI, it's livedata other than that it's unnecessary

    @Query("SELECT ${ActivationCols.ColTerminalId} FROM ${DatabaseInfo.ACTIVATION_TABLE} LIMIT 1")
    fun getTerminalId(): LiveData<String?>

    @Query("SELECT ${ActivationCols.ColIPNo} FROM ${DatabaseInfo.ACTIVATION_TABLE} LIMIT 1")
    fun getHostIP(): LiveData<String?>

    @Query("SELECT ${ActivationCols.ColPortNo} FROM ${DatabaseInfo.ACTIVATION_TABLE} LIMIT 1")
    fun getHostPort(): LiveData<String?>
}