package com.tokeninc.sardis.application_template.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log

class TransactionDB(context: Context?) : DatabaseHelper(context) {

    private var tblTransaction: Map<String, String>? = null
    private var sDatabaseHelper: TransactionDB? = null

    override fun onCreate(db: SQLiteDatabase?) {
        if (db!= null)
            initTransactionTable(db)

    }


    override fun getTableName(): String? {
        return DatabaseInfo.TRANSACTIONTABLE
    }

    fun getInstance(context: Context?): TransactionDB? {
        if (sDatabaseHelper == null) {
            sDatabaseHelper = TransactionDB(context)
            initTransactionTable(writableSQLite!!)
        }
        return sDatabaseHelper
    }

    private fun initTransactionTable(db: SQLiteDatabase) {
        tblTransaction = LinkedHashMap<String, String>()
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_Aid.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_AidLabel.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_AuthCode.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_BatchNo.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_CardReadType.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_CustName.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_ExpDate.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_HostLogKey.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_InstCnt.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_IsVoid.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_ReceiptNo.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_Track2.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_TranDate.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_TransCode.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_UIInstAmount.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_Uuid.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_UISTN.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_UIGUP_SN.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_UIAmount.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_UIAmount2.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_PAN.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_CardSequenceNumber.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_VoidDateTime.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_TextPrintCode1.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_TextPrintCode2.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_DisplayData.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_KeySequenceNumber.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_AC.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_CID.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_ATC.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_TVR.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_TSI.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_AIP.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_CVM.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_AID2.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_UN.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_IAD.name] = "TEXT"
        DatabaseOperations.createTable(DatabaseInfo.TRANSACTIONTABLE, tblTransaction, db)
    }

    /**
     * it needs to called with parameter content values
     */
    fun insertContentVal(values: ContentValues){
            DatabaseOperations.insert( DatabaseInfo.TRANSACTIONTABLE,writableSQLite , values)
    }

    fun updateContentVal(values: ContentValues){
        DatabaseOperations.update(writableSQLite,DatabaseInfo.TRANSACTIONTABLE,"1=1",values)
    }

    /**
     * it needs to called with parameter TransactionCol.<requestedColumnName>.name
     */
    fun getColumn(columnName: String): String? {
        val query = "SELECT " + columnName + " FROM " + DatabaseInfo.TRANSACTIONTABLE + " LIMIT 1"
        return DatabaseOperations.query(readableSQLite, query)
    }

}