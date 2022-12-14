package com.tokeninc.sardis.application_template.database.transaction

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.tokeninc.sardis.application_template.database.DatabaseHelper
import com.tokeninc.sardis.application_template.database.DatabaseInfo
import com.tokeninc.sardis.application_template.database.DatabaseOperations

class TransactionDB(context: Context?) : DatabaseHelper(context) {

    private var tblTransaction: Map<String, String>? = null
    private var sDatabaseHelper: TransactionDB? = null

    override fun onCreate(db: SQLiteDatabase?) {
        if (db!= null)
            initTransactionTable(db)

    }

    override fun getTableName(): String {
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
        tblTransaction = LinkedHashMap()
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_Aid.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_AidLabel.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_AuthCode.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_BatchNo.name] = "INTEGER NOT NULL"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_CardReadType.name] = "INTEGER"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_CustName.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_ExpDate.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_HostLogKey.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_InstCnt.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_IsVoid.name] = "INTEGER"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_isPinByPass.name] = "INTEGER"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_isOffline.name] = "INTEGER"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_ReceiptNo.name] = "INTEGER NOT NULL"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_Track2.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_TranDate.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_TranDate2.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_TransCode.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_InstAmount.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_UUID.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_STN.name] = "INTEGER NOT NULL"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_GUP_SN.name] = "INTEGER NOT NULL UNIQUE"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_Amount.name] = "INTEGER"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_Amount2.name] = "INTEGER"
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

    fun insertTransaction(contentValues: ContentValues?): Boolean {
        return insert(contentValues)
    }

    fun updateContentVal(values: ContentValues){
        DatabaseOperations.update(writableSQLite, DatabaseInfo.TRANSACTIONTABLE, "1=1", values)
    }

    /**
     * it needs to called with parameter TransactionCol.<requestedColumnName>.name
     */
    fun getColumn(columnName: String): String? { 
        val query = "SELECT " + columnName + " FROM " + DatabaseInfo.TRANSACTIONTABLE + " LIMIT 1"
        return DatabaseOperations.query(readableSQLite, query)
    }
}
