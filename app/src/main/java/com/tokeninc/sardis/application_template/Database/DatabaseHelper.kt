package com.tokeninc.sardis.application_template.Database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.jetbrains.annotations.Nullable


abstract class DatabaseHelper:SQLiteOpenHelper {

    constructor(@Nullable context: Context?): super(context,"database.db" , null, 1){
    }

    protected var DATABASE = "database.db"
    protected var ACT_TABLE = "act_table"
    var act_db : String? = null


    override fun onCreate(db: SQLiteDatabase) {
        act_db =
            "CREATE TABLE IF NOT EXISTS " + ACT_TABLE + "(" +
                    ActivationCol.colMerchantId.name + " Text, " +
                    ActivationCol.colTerminalId.name + " Text, " +
                    ActivationCol.colIPNo.name + " Text, " + ActivationCol.colPortNo.name+ " Text);"
        db.execSQL(act_db)
        CheckTxInfoTableIsEmpty()
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //db!!.execSQL("DROP TABLE IF EXISTS " + SALE_TABLE + " ;")
    }

    fun createTables() {
        val db = this.writableDatabase
        act_db =
            "CREATE TABLE IF NOT EXISTS " + ACT_TABLE +
                    "(" + ActivationCol.colMerchantId.name + " Text, " +
                    ActivationCol.colTerminalId.name + " Text);"
        db.execSQL(act_db)
        CheckTxInfoTableIsEmpty()
    }

    fun CheckTxInfoTableIsEmpty() {
        CheckActivationTableIsEmpty()
    }

    fun CheckTableIsEmpty(): Boolean {
        var empty = true
        val db = this.readableDatabase
        val cur = db.rawQuery("SELECT COUNT(*) FROM sale_table", null)
        if (cur != null && cur.moveToFirst()) {
            empty = cur.getInt(0) == 0
        }
        cur!!.close()
        db.close()
        return empty
    }

    fun query(queryStr: String?): String? {
        val db = this.writableDatabase
        val cursor = db.rawQuery(queryStr, null)
        var result: String? = null
        if (cursor.moveToFirst()) {
            result = cursor.getString(0)
        }
        cursor.close()
        return result
    }

    fun update(
        db: SQLiteDatabase,
        tableName: String?,
        whereClause: String?,
        values: ContentValues?
    ): Int {
        return db.update(tableName, values, whereClause, null)
    }

    abstract fun CheckActivationTableIsEmpty()

    abstract fun getMerchantId(): String?

    abstract fun updateMerchantId(merchant_id: String?)

    abstract fun getTerminalId(): String?

    abstract fun updateTerminalId(terminal_id: String?)

    abstract fun getIP_NO(): String?

    abstract fun updateIP_NO(ip_no: String?)

    abstract fun getPort(): String?

    abstract fun updatePort(port_no: String?)


}