package com.tokeninc.sardis.application_template.Database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import org.jetbrains.annotations.Nullable

class ActivationDB: DatabaseHelper {

    constructor(@Nullable  _context: Context?): super(_context) {
    }

    override fun onCreate(db: SQLiteDatabase) {
        super.onCreate(db)
    }

    override fun CheckActivationTableIsEmpty() {
        try {
            val db = this.writableDatabase
            val contentValues2 = ContentValues()
            // No activation check in app temp. That's why MID, TID, IP and PORT information is filled in, when the application is loaded.
            val terminal_id = getTerminalId().toString()
            val merchant_id = getMerchantId().toString()
            val ip_no = getIP_NO().toString()
            val port_no = getPort().toString()
            if (terminal_id == "" || terminal_id == "null" && merchant_id == "" || merchant_id == "null" && ip_no == "" || ip_no == "null" && port_no == "" || port_no == "null") {
                contentValues2.put(ActivationCol.colTerminalId.name, "000005BB")
                contentValues2.put(ActivationCol.colMerchantId.name, "52487539624")
                contentValues2.put(ActivationCol.colIPNo.name, "195.87.189.169")
                contentValues2.put(ActivationCol.colPortNo.name, "1051")
                db.insert(ACT_TABLE, null, contentValues2)
            }
            db.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun getMerchantId(): String? {
        val query = getMerchantIdQuery()
        return query(query)
    }

    override fun getTerminalId(): String? {
        val query = getTerminalIdQuery()
        return query(query)
    }

    override fun getIP_NO(): String? {
        val query = getIPNoQuery()
        return query(query)
    }

    override fun getPort(): String? {
        val query = getPortQuery()
        return query(query)
    }


    override fun updateMerchantId(merchant_id: String?) {
        try {
            val db = this.writableDatabase
            val v = ContentValues()
            v.put(ActivationCol.colMerchantId.name, merchant_id)
            update(db, ACT_TABLE, null, v)
            db.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun updateTerminalId(terminal_id: String?) {
        try {
            val db = this.writableDatabase
            val v = ContentValues()
            v.put(ActivationCol.colTerminalId.name, terminal_id)
            update(db, ACT_TABLE, null, v)
            db.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun updateIP_NO(ip_no: String?) {
        try {
            val db = this.writableDatabase
            val v = ContentValues()
            v.put(ActivationCol.colIPNo.name, ip_no)
            update(db, ACT_TABLE, null, v)
            db.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun updatePort(port_no: String?) {
        try {
            val db = this.writableDatabase
            val v = ContentValues()
            v.put(ActivationCol.colPortNo.name, port_no)
            update(db, ACT_TABLE, null, v)
            //db.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun createActivationTableQuery(): String? {
        return "CREATE TABLE IF NOT EXISTS " + ACT_TABLE + "(" +
                ActivationCol.colMerchantId.name + " Text, " +
                ActivationCol.colTerminalId.name + " Text);"
    }

    fun onCreateActivationTableQuery(): String? {
        return "CREATE TABLE IF NOT EXISTS " + ACT_TABLE + "(" +
                ActivationCol.colMerchantId.name + " Text, " +
                ActivationCol.colTerminalId.name +
                " Text, " + ActivationCol.colIPNo.name + " Text, " +
                ActivationCol.colPortNo.name + " Text);"
    }

    fun getMerchantIdQuery(): String? {
        return "SELECT " + ActivationCol.colMerchantId.name + " FROM " + ACT_TABLE + " LIMIT 1"
    }

    fun getTerminalIdQuery(): String? {
        return "SELECT " + ActivationCol.colTerminalId.name + " FROM " + ACT_TABLE + " LIMIT 1"
    }

    fun getIPNoQuery(): String?{
        return "SELECT " + ActivationCol.colIPNo.name + " FROM " + ACT_TABLE + " LIMIT 1"
    }

    fun getPortQuery(): String?{
        return "SELECT " + ActivationCol.colPortNo.name + " FROM " + ACT_TABLE + " LIMIT 1"
    }

    fun updateMerchantID(merchant_id: String?) {
        Log.w("Merch/Activation", "")
        try {
            val db = this.writableDatabase
            val v = ContentValues()
            v.put(ActivationCol.colMerchantId.name, merchant_id)
            v.put(ActivationCol.colPortNo.name, "1051")
            update(db, ACT_TABLE, null, v)
            db.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateTerminalID(terminal_id: String?){
        Log.w("Terminal/Act","")
        try {
            val db = this.writableDatabase
            val v = ContentValues()
            v.put(ActivationCol.colTerminalId.name, terminal_id)
            update(db, ACT_TABLE, null, v)
            db.close()
        } catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun updateIP_No(ip_no: String?){
        Log.w("IP/Act","")
        try {
            val db = this.writableDatabase
            val v = ContentValues()
            v.put(ActivationCol.colIPNo.name, ip_no)
            update(db, ACT_TABLE, null, v)
            db.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updatePortNo(port_no: String?){
        Log.w("Port/Act","")
        try {
            val db = this.writableDatabase
            val v = ContentValues()
            v.put(ActivationCol.colPortNo.name, port_no)
            update(db, ACT_TABLE, null, v)
            //db.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }




}