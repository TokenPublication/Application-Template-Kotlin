package com.tokeninc.sardis.application_template.database.entities

import android.provider.ContactsContract.Data
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tokeninc.sardis.application_template.database.DatabaseInfo
import com.tokeninc.sardis.application_template.database.activation.ActivationCol

@Dao
interface ActivationDao {

    @Query("UPDATE ${DatabaseInfo.ACTTABLE} SET ${ActivationCols.ColIPNo} = :ip, ${ActivationCols.ColPortNo} = :port WHERE ${ActivationCols.ColIPNo} = :old_ip")
    suspend fun updateConnection(ip: String?, port: String?, old_ip: String?) //users should get old_ip with getHostPort method

    @Query("UPDATE ${DatabaseInfo.ACTTABLE} SET ${ActivationCols.ColTerminalId} = :terminalId, ${ActivationCols.ColMerchantId} = :merchantId WHERE ${ActivationCols.ColIPNo} = :ip")
    suspend fun updateActivation(terminalId: String?, merchantId: String?, ip: String?)

    @Query("SELECT ${ActivationCols.ColMerchantId} FROM ${DatabaseInfo.ACTTABLE} LIMIT 1")
    fun getMerchantId(): String?

    @Query("SELECT ${ActivationCols.ColTerminalId} FROM ${DatabaseInfo.ACTTABLE} LIMIT 1")
    fun getTerminalId(): String?

    @Query("SELECT ${ActivationCols.ColIPNo} FROM ${DatabaseInfo.ACTTABLE} LIMIT 1")
    fun getHostIP(): String?

    @Query("SELECT ${ActivationCols.ColPortNo} FROM ${DatabaseInfo.ACTTABLE} LIMIT 1")
    fun getHostPort(): String?

    @Query("DELETE FROM ${DatabaseInfo.ACTTABLE}")
    fun deleteAll()

}