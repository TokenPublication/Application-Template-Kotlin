package com.tokeninc.sardis.application_template.database.activation

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.tokeninc.sardis.application_template.database.DatabaseHelper
import com.tokeninc.sardis.application_template.database.DatabaseInfo
import com.tokeninc.sardis.application_template.database.DatabaseOperations
/**

/**
 * This class includes methods of Activation table, it inherits from databaseHelper.
 */
class ActivationDB(context: Context?) : DatabaseHelper(context) {

    private var sDatabaseHelper: ActivationDB? = null
    private var tblActivation: Map<String, String>? = null
    private val ip = "195.87.189.169"
    private val port = "1000"


    /**
     * Setting columns of table
     */
    private fun initActivationTable(db: SQLiteDatabase) {
        tblActivation = LinkedHashMap()
        (tblActivation as LinkedHashMap<String, String>)[ActivationCol.ColTerminalId.name] = "TEXT"
        (tblActivation as LinkedHashMap<String, String>)[ActivationCol.ColMerchantId.name] = "TEXT"
        (tblActivation as LinkedHashMap<String, String>)[ActivationCol.ColIPNo.name] = "TEXT"
        (tblActivation as LinkedHashMap<String, String>)[ActivationCol.ColPortNo.name] = "TEXT"
        DatabaseOperations.createTable(DatabaseInfo.ACTTABLE,
            tblActivation as LinkedHashMap<String, String>, db)
    }

    override fun getTableName(): String {
        return DatabaseInfo.ACTTABLE
    }

    fun getInstance(context: Context?): ActivationDB? {
        if (sDatabaseHelper == null) {
            sDatabaseHelper = ActivationDB(context)
            initActivationTable(writableSQLite!!)
            sDatabaseHelper!!.initHostSettings()
        }
        return sDatabaseHelper
    }

    override fun onCreate(db: SQLiteDatabase?) {
        if (db != null) {
            initActivationTable(db)
        }
    }

    /**
     * Inserting connection with IP and port.
     */
    fun insertConnection(IP: String?, port: String?): Boolean {
        val values = ContentValues()
        values.put(ActivationCol.ColIPNo.name, IP)
        values.put(ActivationCol.ColPortNo.name, port)
        DatabaseOperations.deleteAllRecords(DatabaseInfo.ACTTABLE, writableSQLite!!)
        return DatabaseOperations.insert(DatabaseInfo.ACTTABLE, writableSQLite!!, values)
    }

    /**
     * Updating IP and Port numbers from settings menu > Host settings.
     */
    fun updateConnection(IP: String?, port: String?) {
        val values = ContentValues()
        values.put(ActivationCol.ColIPNo.name, IP)
        values.put(ActivationCol.ColPortNo.name, port)
        DatabaseOperations.update(writableSQLite!!, DatabaseInfo.ACTTABLE, null, values)
    }

    /**
     * Updating Activation with both terminal and merchant ID from setting menu > TID MID fragment
     */
    fun updateActivation(terminalId: String?, merchantId: String?) {
        val values = ContentValues()
        values.put(ActivationCol.ColTerminalId.name, terminalId)
        values.put(ActivationCol.ColMerchantId.name, merchantId)
        DatabaseOperations.update(writableSQLite!!, DatabaseInfo.ACTTABLE, null, values)
        //DatabaseOperations.update(writableSQLite!!, DatabaseInfo.ACTTABLE, "1=1", values)
    }

    /**
     * If table is empty, it automatically gives ip and port number as a default values.
     */
    private fun initHostSettings() {
        val count = try {
            DatabaseOperations.query(
                readableSQLite!!, "SELECT COUNT(*) FROM " + DatabaseInfo.ACTTABLE).toInt()
        } catch (e:Exception) {
            0
        }
        if (count <= 0) {
            insertConnection(ip, port)
        }
    }



    fun getMerchantId(): String? {
        val query = "SELECT " + ActivationCol.ColMerchantId.name + " FROM " + DatabaseInfo.ACTTABLE + " LIMIT 1"
        return DatabaseOperations.query(readableSQLite!!, query)
    }

    fun getTerminalId(): String? {
        val query = "SELECT " + ActivationCol.ColTerminalId.name + " FROM " + DatabaseInfo.ACTTABLE + " LIMIT 1"
        return DatabaseOperations.query(readableSQLite!!, query)
    }

    fun getHostIP(): String? {
        val query = "SELECT " + ActivationCol.ColIPNo.name + " FROM " + DatabaseInfo.ACTTABLE + " LIMIT 1"
        return DatabaseOperations.query(readableSQLite!!, query)
    }

    fun getHostPort(): String? {
        val query = "SELECT " + ActivationCol.ColPortNo.name + " FROM " + DatabaseInfo.ACTTABLE + " LIMIT 1"
        return DatabaseOperations.query(readableSQLite!!, query)
    }
}
*/