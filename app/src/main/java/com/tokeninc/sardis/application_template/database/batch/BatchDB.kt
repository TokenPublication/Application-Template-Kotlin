package com.tokeninc.sardis.application_template.database.batch
/**
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.tokeninc.sardis.application_template.database.DatabaseHelper
import com.tokeninc.sardis.application_template.database.DatabaseInfo
import com.tokeninc.sardis.application_template.database.DatabaseOperations

/**
 * This is a class that holds methods of Batch Table, it is inherited from DatabaseHelper
 */
/**
class BatchDB(context: Context?) : DatabaseHelper(context) {


    private var sDatabaseHelper: BatchDB? = null
    private var tblBatch: Map<String, String>? = null

    /**
     * Initializing the table
     */
    private fun initBatchTable(db: SQLiteDatabase) {
        tblBatch = LinkedHashMap()
        (tblBatch as HashMap<String, String>)[BatchCol.col_ulGUP_SN.name] = "INTEGER DEFAULT 0"
        (tblBatch as HashMap<String, String>)[BatchCol.col_batchNo.name] = "INTEGER DEFAULT 1"
        DatabaseOperations.createTable(DatabaseInfo.BATCHTABLE,
            tblBatch as LinkedHashMap<String, String>, db)
    }

    override fun getTableName(): String {
        return DatabaseInfo.ACTTABLE
    }

    fun getInstance(context: Context?): BatchDB? {
        if (sDatabaseHelper == null) {
            sDatabaseHelper = BatchDB(context)
            initBatchTable(writableSQLite!!)
            sDatabaseHelper!!.initBatch()
        }
        return sDatabaseHelper
    }

    override fun onCreate(db: SQLiteDatabase?) {
        if (db != null) {
            initBatchTable(db)
        }
    }

    /**
     * If table is empty initialize values via insertBatch() function
     */
    private fun initBatch() {
        val count = try {
            DatabaseOperations.query(readableSQLite!!,"SELECT COUNT(*) FROM " + DatabaseInfo.BATCHTABLE).toInt()
        } catch (e:Exception) {
            0
        }
        if (count <= 0) {
            insertBatch()
        }
    }

    /**
     * When table is empty it is called for initializing batch number as 1 and group serial number as 0 with deleting all records.
     */
    private fun insertBatch(): Boolean {
        val values = ContentValues()
        values.put(BatchCol.col_ulGUP_SN.name, 0)
        values.put(BatchCol.col_batchNo.name, 1)
        DatabaseOperations.deleteAllRecords(DatabaseInfo.BATCHTABLE, writableSQLite!!)
        return DatabaseOperations.insert(DatabaseInfo.BATCHTABLE, writableSQLite!!, values)
    }

    /**
     * It is updating Group Serial number as increasing 1.
     */
    fun updateGUPSN(): Int {
        var sn = getGUPSN()!!
        val v = ContentValues()
        if (sn == null) {
            sn = 0
            v.put(BatchCol.col_ulGUP_SN.name, ++sn)
            if (DatabaseOperations.insert(DatabaseInfo.BATCHTABLE, writableSQLite!!, v)) {
                return sn
            }
        } else {
            v.put(BatchCol.col_ulGUP_SN.name, ++sn)
            DatabaseOperations.update(writableSQLite!!, DatabaseInfo.BATCHTABLE, null, v)
        }
        return sn
    }

    /**
     * It reset group serial number as 0 for the times when batch is closing.
     */
    private fun resetGUPSN() {
        val v = ContentValues()
        v.put(BatchCol.col_ulGUP_SN.name, 0)
        DatabaseOperations.update(writableSQLite!!, DatabaseInfo.BATCHTABLE, null, v)
    }


    /**
     * Getting group serial number
     */
    fun getGUPSN(): Int? {
        val query = "SELECT " + BatchCol.col_ulGUP_SN.name + " FROM " + DatabaseInfo.BATCHTABLE + " LIMIT 1"
        val sn = DatabaseOperations.query(readableSQLite!!, query)
        return if (sn == null) null else Integer.valueOf(sn)
    }

    /**
     * Updating batch number by increasing 1 for after batch closing
     */
    fun updateBatchNo(batchNo: Int) {
        val v = ContentValues()
        v.put(BatchCol.col_batchNo.name, batchNo)
        DatabaseOperations.update(writableSQLite!!, DatabaseInfo.BATCHTABLE, null, v)
        resetGUPSN()
    }

    /**
     * Getting batch number
     */
    fun getBatchNo(): Int? {
        val query = "SELECT " + BatchCol.col_batchNo.name + " FROM " + DatabaseInfo.BATCHTABLE + " LIMIT 1"
        val no = DatabaseOperations.query(readableSQLite!!, query)
        return if (no == null) 0 else Integer.valueOf(no)
    }
}
 */