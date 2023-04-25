package com.tokeninc.sardis.application_template.database.slip

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.tokeninc.sardis.application_template.database.DatabaseHelper
import com.tokeninc.sardis.application_template.database.DatabaseInfo
import com.tokeninc.sardis.application_template.database.DatabaseOperations
/**

class SlipDB(context: Context): DatabaseHelper(context) {

    private var tblActivation: Map<String, String>? = null
    private var sDatabaseHelper: SlipDB? = null

    override fun getTableName(): String? {
        return DatabaseInfo.SLIPTABLE
    }

    override fun onCreate(db: SQLiteDatabase?) {
        if (db != null) {
            initSlipTable(db)
        }
    }

    private fun initSlipTable(db: SQLiteDatabase) {
        tblActivation = LinkedHashMap()
        (tblActivation as LinkedHashMap<String, String>)[SlipCol.col_Slip.name] = "TEXT"
        DatabaseOperations.createTable(
            DatabaseInfo.SLIPTABLE,
            tblActivation as LinkedHashMap<String, String>, db
        )
    }

    fun getInstance(context: Context?): SlipDB? {
        if (sDatabaseHelper == null) {
            sDatabaseHelper = SlipDB(context!!)
            initSlipTable(writableSQLite!!)
        }
        return sDatabaseHelper
    }

    fun insertSlip(slip: String?): Boolean {
        val values = ContentValues()
        values.put(SlipCol.col_Slip.name, slip)
        DatabaseOperations.deleteAllRecords(DatabaseInfo.SLIPTABLE, writableSQLite!!)
        return DatabaseOperations.insert(DatabaseInfo.SLIPTABLE, writableSQLite!!, values)
    }


    fun getSlip(): String? {
        val query = "SELECT " + SlipCol.col_Slip + " FROM " + DatabaseInfo.SLIPTABLE + " LIMIT 1"
        return DatabaseOperations.query(readableSQLite!!, query)
    }

}
 */