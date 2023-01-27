package com.tokeninc.sardis.application_template.database.transaction

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.tokeninc.sardis.application_template.database.DatabaseHelper
import com.tokeninc.sardis.application_template.database.DatabaseInfo
import com.tokeninc.sardis.application_template.database.DatabaseOperations
import com.tokeninc.sardis.application_template.entities.ICCCard
import java.text.SimpleDateFormat
import java.util.*

/**
 * This is a class that holds Transaction Table's operations. It is inherited from Database Helper.
 */
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

    /**
     * If tables hasn't been created before, now it is created.
     */
    fun getInstance(context: Context?): TransactionDB? {
        if (sDatabaseHelper == null) {
            sDatabaseHelper = TransactionDB(context)
            initTransactionTable(writableSQLite!!)
        }
        return sDatabaseHelper
    }

    /** Initializing table for creating.
     * All of its columns are created here with their types.
     */
    private fun initTransactionTable(db: SQLiteDatabase) {
        tblTransaction = LinkedHashMap()
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_GUP_SN.name] = "INTEGER NOT NULL UNIQUE"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_Aid.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_AidLabel.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_AuthCode.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_BatchNo.name] = "INTEGER DEFAULT 0"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_CardReadType.name] = "INTEGER"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_CustName.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_ExpDate.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_HostLogKey.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_InstCnt.name] = "INTEGER DEFAULT 0"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_IsVoid.name] = "INTEGER"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_isPinByPass.name] = "INTEGER"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_isOffline.name] = "INTEGER"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_ReceiptNo.name] = "INTEGER NOT NULL"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_Track2.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_TranDate.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_TransCode.name] = "INTEGER DEFAULT 0"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_InstAmount.name] = "INTEGER DEFAULT 0"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_UUID.name] = "TEXT"
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
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_SID.name] = "TEXT"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_Ext_Conf.name] = "INTEGER DEFAULT 0"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_Ext_Ref.name] = "INTEGER DEFAULT 0"
        (tblTransaction as LinkedHashMap<String, String>)[TransactionCol.Col_Ext_RefundDateTime.name] = "TEXT"
        DatabaseOperations.createTable(DatabaseInfo.TRANSACTIONTABLE,
            tblTransaction as LinkedHashMap<String, String>, db)
    }

    /**
     * Inserting transaction with DatabaseHelper's method.
     */
    fun insertTransaction(contentValues: ContentValues?): Boolean {
        return insert(contentValues)
    }

    fun updateContentVal(values: ContentValues){
        DatabaseOperations.update(writableSQLite!!, DatabaseInfo.TRANSACTIONTABLE, "1=1", values)
    }

    /**
     * Deleting all transactions in the table for batch close operation.
     */
    fun deleteAll(){
        DatabaseOperations.deleteAllRecords(DatabaseInfo.TRANSACTIONTABLE, writableSQLite!!)
    }

    /**
     * it needs to called with parameter TransactionCol.<requestedColumnName>.name
     */
    fun getColumn(columnName: String): String? { 
        val query = "SELECT " + columnName + " FROM " + DatabaseInfo.TRANSACTIONTABLE + " LIMIT 1"
        return DatabaseOperations.query(readableSQLite!!, query)
    }

    /**
     * It is getting transactions with given card Number for Void operation.
     */
    fun getTransactionsByCardNo(cardNo: String): List<ContentValues?> {
        return selectTransaction("SELECT * FROM " + DatabaseInfo.TRANSACTIONTABLE + " WHERE " + TransactionCol.Col_PAN.name + "='" + cardNo + "' AND " + TransactionCol.Col_IsVoid.name + " <> '1' ORDER BY " + TransactionCol.Col_GUP_SN.name + " DESC")
    }

    /**
     * It is getting transactions with given Reference number for Refund operation
     */
    fun getTransactionsByRefNo(refNo: String): List<ContentValues?> {
        return selectTransaction("SELECT * FROM " + DatabaseInfo.TRANSACTIONTABLE + " WHERE " + TransactionCol.Col_HostLogKey.name + "='" + refNo +"'")
    }

    /**
     * It gets all transactions for batch close operation.
     */
    fun getAllTransactions(): List<ContentValues?> {
        return selectTransaction("SELECT * FROM " + DatabaseInfo.TRANSACTIONTABLE + " ORDER BY " + TransactionCol.Col_GUP_SN.name)
    }

    /**
     * It selects a transaction from database with a given query.
     */
    private fun selectTransaction(queryStr: String): List<ContentValues> {
        val cursor = readableSQLite!!.rawQuery(queryStr, null)
        val rows: MutableList<ContentValues> = ArrayList()
        if (cursor.moveToFirst()) {
            do {
                val contentValues = ContentValues()
                val colNames: Array<TransactionCol> = TransactionCol.values()
                for (colName in colNames) {
                    addColumnValue(contentValues, cursor, colName.name)
                }
                rows.add(contentValues)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return rows
    }

    /**
     * It is for Void operation, it updates the sale transaction with void parameters.
     */
    fun setVoid(gupSN: Int, date: String?, card: ICCCard): Int {
        val values = ContentValues()
        values.put(TransactionCol.Col_IsVoid.name, 1)
        values.put(TransactionCol.Col_VoidDateTime.name, date)
        values.put(TransactionCol.Col_SID.name, card.SID)
        val retval = DatabaseOperations.update(writableSQLite, DatabaseInfo.TRANSACTIONTABLE, TransactionCol.Col_GUP_SN.name + " = " + gupSN, values)
        return retval
    }


}
