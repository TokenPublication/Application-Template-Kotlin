package com.tokeninc.sardis.application_template.database.batch

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.tokeninc.sardis.application_template.database.DatabaseHelper
import com.tokeninc.sardis.application_template.database.DatabaseInfo
import com.tokeninc.sardis.application_template.database.DatabaseOperations

class BatchDB(context: Context?) : DatabaseHelper(context) {

    private var sDatabaseHelper: BatchDB? = null
    private var tblBatch: Map<String, String>? = null

    private fun initBatchTable(db: SQLiteDatabase) {
        tblBatch = HashMap()
        (tblBatch as HashMap<String, String>)[BatchCol.col_ulSTN.name] = "INTEGER DEFAULT 0"
        (tblBatch as HashMap<String, String>)[BatchCol.col_ulGUP_SN.name] = "INTEGER DEFAULT 0"
        (tblBatch as HashMap<String, String>)[BatchCol.col_batchNo.name] = "INTEGER DEFAULT 1"
        (tblBatch as HashMap<String, String>)[BatchCol.col_settleNo.name] = "TEXT"
        DatabaseOperations.createTable(DatabaseInfo.BATCHTABLE, tblBatch, db)
    }

    override fun getTableName(): String {
        return DatabaseInfo.ACTTABLE
    }

    fun getInstance(context: Context?): BatchDB? {
        if (sDatabaseHelper == null) {
            sDatabaseHelper = BatchDB(context)
            initBatchTable(writableSQLite!!)
            sDatabaseHelper!!.initHostSettings()
        }
        return sDatabaseHelper
    }

    override fun onCreate(db: SQLiteDatabase?) {
        if (db != null) {
            initBatchTable(db)
        }
    }

    private fun initHostSettings() {
        val count = try {
            DatabaseOperations.query(readableSQLite,"SELECT COUNT(*) FROM $" + DatabaseInfo.BATCHTABLE).toInt()
        } catch (e:Exception) {
            0
        }
        if (count <= 0) {
            insertBatch()
        }
    }

    private fun insertBatch(): Boolean {
        val values = ContentValues()
        values.put(BatchCol.col_ulSTN.name, 0)
        values.put(BatchCol.col_ulGUP_SN.name, 0)
        values.put(BatchCol.col_batchNo.name, 1)
        DatabaseOperations.deleteAllRecords(DatabaseInfo.BATCHTABLE, writableSQLite)
        return DatabaseOperations.insert(DatabaseInfo.BATCHTABLE, writableSQLite, values)
    }

    fun updateSTN(): Int {
        var stn: Int = getSTN()
        val v = ContentValues()
        if (stn == null) {
            stn = 0
            v.put(BatchCol.col_ulSTN.name, ++stn)
            if (DatabaseOperations.insert(DatabaseInfo.BATCHTABLE, writableSQLite, v)) {
                return stn
            }
        } else {
            if (stn == 999) {
                stn = 0
            }
            v.put(BatchCol.col_ulSTN.name, ++stn)
            DatabaseOperations.update(writableSQLite, DatabaseInfo.BATCHTABLE, null, v)
        }
        return stn
    }

    fun getSTN(): Int {
        val query = "SELECT " + BatchCol.col_ulSTN.name + " FROM " + DatabaseInfo.BATCHTABLE + " LIMIT 1"
        val stn = DatabaseOperations.query(readableSQLite, query)
        return if (stn == null) 0 else Integer.valueOf(stn)
    }

    fun getSettleNo(): String? {
        val query = "SELECT " + BatchCol.col_settleNo.name  + " FROM " + DatabaseInfo.BATCHTABLE + " LIMIT 1"
        return DatabaseOperations.query(readableSQLite, query)
    }

    fun setSettleNo(settleNo: String?) {
        val v = ContentValues()
        v.put(BatchCol.col_settleNo.name, settleNo)
        DatabaseOperations.update(writableSQLite, DatabaseInfo.BATCHTABLE, null, v)
    }

    fun updateGUPSN(): Int {
        var sn = getGUPSN()!!
        val v = ContentValues()
        if (sn == null) {
            sn = 0
            v.put(BatchCol.col_ulGUP_SN.name, ++sn)
            if (DatabaseOperations.insert(DatabaseInfo.BATCHTABLE, writableSQLite, v)) {
                return sn
            }
        } else {
            v.put(BatchCol.col_ulGUP_SN.name, ++sn)
            DatabaseOperations.update(writableSQLite, DatabaseInfo.BATCHTABLE, null, v)
        }
        return sn
    }

    fun resetGUPSN() {
        val v = ContentValues()
        v.put(BatchCol.col_ulGUP_SN.name, 0)
        DatabaseOperations.update(writableSQLite, DatabaseInfo.BATCHTABLE, null, v)
    }

    fun resetSTN() {
        val v = ContentValues()
        v.put(BatchCol.col_ulSTN.name, 0)
        DatabaseOperations.update(writableSQLite, DatabaseInfo.BATCHTABLE, null, v)
    }

    fun getGUPSN(): Int? {
        val query = "SELECT " + BatchCol.col_ulGUP_SN.name + " FROM " + DatabaseInfo.BATCHTABLE + " LIMIT 1"
        val sn = DatabaseOperations.query(readableSQLite, query)
        return if (sn == null) 1 else Integer.valueOf(sn)
    }

    fun updateBatchNo(batchNo: Int) {
        val v = ContentValues()
        v.put(BatchCol.col_batchNo.name, batchNo)
        DatabaseOperations.update(writableSQLite, DatabaseInfo.BATCHTABLE, null, v)
    }

    fun getBatchNo(): Int? {
        val query = "SELECT " + BatchCol.col_batchNo.name + " FROM " + DatabaseInfo.BATCHTABLE + " LIMIT 1"
        val no = DatabaseOperations.query(readableSQLite, query)
        return if (no == null) 0 else Integer.valueOf(no)
    }
}
