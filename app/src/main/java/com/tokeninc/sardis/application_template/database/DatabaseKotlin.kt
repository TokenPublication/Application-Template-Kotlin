package com.tokeninc.sardis.application_template.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log

/**
class DatabaseKotlin {

    /**
     * Method creates a table with specified tableName
     * Columns and constraints must be given in a Map
     *
     * @param tableName Name of the table to be created
     * @param columns Column names as keys and column constraints as values
     * @param sqLiteDatabase SQLiteDatabase instance
     * @return
     */
    fun createTable(
        tableName: String,
        columns: HashMap<String, String>,
        sqLiteDatabase: SQLiteDatabase
    ) {
        val createTableQuery = StringBuilder()
        createTableQuery.append("CREATE TABLE IF NOT EXISTS $tableName (")
        val iterator: MutableIterator<*> = columns.entries.iterator()
        while (iterator.hasNext()) {
            val (key, value) = iterator.next() as Map.Entry<*, *>
            createTableQuery.append(key.toString() + " " + value)
            iterator.remove()
            if (iterator.hasNext()) {
                createTableQuery.append(",")
            } else {
                createTableQuery.append(");")
            }
        }
        Log.d("CreateTable", createTableQuery.toString())
        sqLiteDatabase.execSQL(createTableQuery.toString())
    }

    fun createTable(
        tableName: String,
        columns: MutableMap<String?, String?>,
        uniqueVal1: String?,
        uniqueVal2: String?,
        sqLiteDatabase: SQLiteDatabase
    ) {
        val createTableQuery = StringBuilder()
        createTableQuery.append("CREATE TABLE IF NOT EXISTS $tableName (")
        val iterator: MutableIterator<*> = columns.entries.iterator()
        while (iterator.hasNext()) {
            val (key, value) = iterator.next() as Map.Entry<*, *>
            createTableQuery.append(key.toString() + " " + value)
            iterator.remove()
            if (iterator.hasNext()) {
                createTableQuery.append(",")
            } else {
                createTableQuery.append(" ,UNIQUE (").append(uniqueVal1).append(", ")
                    .append(uniqueVal2).append(") ON CONFLICT REPLACE );")
            }
        }
        Log.d("CreateUniqueTable", createTableQuery.toString())
        sqLiteDatabase.execSQL(createTableQuery.toString())
    }

    /**
     *
     * @param tableName Name of the table
     * @param sqLiteDatabase Writable SQLiteDatabase Instance
     * @param contentValues Map that contains values to be inserted, Column names as keys
     * @return Id of the row that was inserted if successful, false otherwise
     */
    /*
    fun insert(
        tableName: String?,
        sqLiteDatabase: SQLiteDatabase,
        contentValues: ContentValues?
    ): Boolean {
        val rowId = sqLiteDatabase.insert(tableName, null, contentValues)
        return rowId != -1L // -1 geliyordu o yüzden buna döndürdü
    }

     */

    fun replace(
        tableName: String?,
        sqLiteDatabase: SQLiteDatabase,
        contentValues: ContentValues?
    ): Boolean {
        val rowId = sqLiteDatabase.replace(tableName, null, contentValues)
        return rowId != -1L
    }

    fun query(db: SQLiteDatabase, queryStr: String?): String {
        val cursor = db.rawQuery(queryStr, null)
        lateinit var result: String
        if (cursor.moveToFirst()) {
            result = cursor.getString(0)
        }
        cursor.close()
        return result
    }

    fun query(
        db: SQLiteDatabase,
        tableName: String,
        columns: String,
        caseList: List<String?>
    ): String? {
        val queryString = StringBuilder("SELECT $columns FROM $tableName")
        if (caseList.size > 0) {
            queryString.append(" WHERE")
        }
        for (key in caseList) {
            queryString.append(" ")
            queryString.append(key)
            queryString.append(" AND")
        }
        if (queryString.substring(queryString.length - 4) == " AND") {
            queryString.delete(queryString.length - 4, queryString.length)
        }
        return query(db, queryString.toString())
    }
    /*
    fun update(
        db: SQLiteDatabase,
        tableName: String?,
        whereClause: String?,
        values: ContentValues
    ): Int {
        Log.d("DBUpdate", "size: " + values.size())
        return db.update(tableName, values, whereClause, null)
    }
     */


    /**
     *
     * @param tableName
     * @param sqLiteDatabase
     */
    fun dropTable(tableName: String, sqLiteDatabase: SQLiteDatabase) {
        val dropTableQuery = "DROP TABLE IF EXISTS '$tableName';"
        sqLiteDatabase.execSQL(dropTableQuery)
    }

    /**
     *
     * @param tableName Name of the table
     * @param selection Column name to be selected
     * @param selectionArguments
     * @param sqLiteDatabase SQLiteDatabase Instance
     * @return Number of rows that were deleted
     */
    fun deleteRecordsWithCondition(
        tableName: String?,
        selection: String,
        selectionArguments: Array<String?>?,
        sqLiteDatabase: SQLiteDatabase
    ): Int {
        return sqLiteDatabase.delete(
            tableName,
            "$selection LIKE ?", selectionArguments
        )
    }

    /**
     * Deletes all rows from a table
     * @param tableName Name of the table
     * @param sqLiteDatabase SQLiteDatabase Instance
     * @return Number of rows that were deleted
     */
    fun deleteAllRecords(tableName: String?, sqLiteDatabase: SQLiteDatabase): Int {
        var deletedRows = 0
        try {
            deletedRows = sqLiteDatabase.delete(tableName, "1", null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return deletedRows
    }


}
 */