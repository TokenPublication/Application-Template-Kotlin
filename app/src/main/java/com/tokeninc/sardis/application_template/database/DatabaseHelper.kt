package com.tokeninc.sardis.application_template.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.jetbrains.annotations.Nullable
import java.text.SimpleDateFormat
import java.util.*

abstract class DatabaseHelper(@Nullable context: Context?) : SQLiteOpenHelper(context, DatabaseInfo.DATABASENAME, null, DatabaseInfo.DATABASEVERSION) {

    protected var writableSQLite: SQLiteDatabase? = writableDatabase
    protected var readableSQLite: SQLiteDatabase? = readableDatabase

    override fun onConfigure(db: SQLiteDatabase) {
        db.execSQL("PRAGMA synchronous = 2")
    }

    protected open fun replace(tableName: String?, values: ContentValues?): Boolean {
        return DatabaseOperations.replace(tableName, writableSQLite, values)
    }

    protected open fun update(
        tableName: String?,
        values: ContentValues?,
        whereClause: String?
    ): Int {
        return DatabaseOperations.update(
            writableSQLite,
            tableName,
            whereClause,
            values
        )
    }

    protected open fun getDate(): String? {
        return SimpleDateFormat("yyMMdd", Locale.getDefault()).format(Calendar.getInstance().time)
    }

    protected abstract fun getTableName(): String?

    protected open fun insert(values: ContentValues?): Boolean {
        return DatabaseOperations.insert(getTableName(), writableSQLite, values)
    }

    protected open fun addColumnValue(
        contentValues: ContentValues,
        cursor: Cursor,
        colName: String?
    ) {
        val index = cursor.getColumnIndex(colName)
        if (index > -1) {
            if (cursor.getType(index) == Cursor.FIELD_TYPE_INTEGER) {
                contentValues.put(colName, cursor.getInt(index))
            } else if (cursor.getType(index) == Cursor.FIELD_TYPE_STRING) {
                contentValues.put(colName, cursor.getString(index))
            } else {
                contentValues.putNull(colName)
            }
        }
    }

    protected open fun selectRecord(
        queryStr: String?,
        columns: Array<Enum<*>>
    ): List<ContentValues>? {
        val cursor: Cursor = readableSQLite!!.rawQuery(queryStr, null)
        val rows: MutableList<ContentValues> = ArrayList()
        if (cursor.moveToFirst()) {
            do {
                val contentValues = ContentValues()
                for (colName in columns) {
                    addColumnValue(contentValues, cursor, colName.name)
                }
                rows.add(contentValues)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return rows
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // TODO Egecan: Check for onUpgrade later...
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.version = oldVersion
    }

    open fun clearTable() {
        DatabaseOperations.deleteAllRecords(getTableName(), writableSQLite)
    }
}
