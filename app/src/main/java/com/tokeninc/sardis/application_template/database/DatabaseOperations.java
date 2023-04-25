package com.tokeninc.sardis.application_template.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
/**
public class DatabaseOperations {

    /**
     * Method creates a table with specified tableName
     * Columns and constraints must be given in a Map
     *
     * @param tableName Name of the table to be created
     * @param columns Column names as keys and column constraints as values
     * @param sqLiteDatabase SQLiteDatabase instance
     * @return
     */

    // TODO Egecan: Convert to Kotlin
/**
    public static void createTable(String tableName,Map<String,String> columns,SQLiteDatabase sqLiteDatabase){
        StringBuilder createTableQuery = new StringBuilder();
        createTableQuery.append("CREATE TABLE IF NOT EXISTS " + tableName + " (");

        Iterator iterator = columns.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry pair = (Map.Entry)iterator.next();

            createTableQuery.append(pair.getKey() + " " + pair.getValue());
            iterator.remove();

            if(iterator.hasNext()){
                createTableQuery.append(",");
            }else{
                createTableQuery.append(");");
            }
        }

        Log.d("CreateTable",createTableQuery.toString());
        sqLiteDatabase.execSQL(createTableQuery.toString());
    }

    public static void createTable(String tableName,Map<String,String> columns, String uniqueVal1, String uniqueVal2, SQLiteDatabase sqLiteDatabase){
        StringBuilder createTableQuery = new StringBuilder();
        createTableQuery.append("CREATE TABLE IF NOT EXISTS " + tableName + " (");

        Iterator iterator = columns.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry pair = (Map.Entry)iterator.next();

            createTableQuery.append(pair.getKey() + " " + pair.getValue());
            iterator.remove();

            if(iterator.hasNext()){
                createTableQuery.append(",");
            }else{
                createTableQuery.append(" ,UNIQUE (").append(uniqueVal1).append(", ").append(uniqueVal2).append(") ON CONFLICT REPLACE );");
            }
        }

        Log.d("CreateUniqueTable",createTableQuery.toString());
        sqLiteDatabase.execSQL(createTableQuery.toString());
    }

    /**
     *
     * @param tableName Name of the table
     * @param sqLiteDatabase Writable SQLiteDatabase Instance
     * @param contentValues Map that contains values to be inserted, Column names as keys
     * @return Id of the row that was inserted if successful, false otherwise
     */
/**
    public static boolean insert(String tableName, SQLiteDatabase sqLiteDatabase, ContentValues contentValues){
        long rowId = sqLiteDatabase.insert(tableName, null, contentValues);
        return rowId != -1; // -1 geliyordu o yüzden buna döndürdü 1 geldi ilkinde
    }

    public static boolean replace(String tableName, SQLiteDatabase sqLiteDatabase, ContentValues contentValues){
        long rowId = sqLiteDatabase.replace(tableName, null, contentValues);
        return rowId != -1;
    }

    public static String query(SQLiteDatabase db, String queryStr) {
        Cursor cursor = db.rawQuery(queryStr, null);
        String
         result = null;
        if (cursor.moveToFirst()) {
            result = cursor.getString(0);
        }
        cursor.close();
        return result;
    }

    public static String query(SQLiteDatabase db, String tableName, String columns, List<String> caseList) {
        StringBuilder queryString = new StringBuilder("SELECT " + columns + " FROM " + tableName);
        if (caseList.size() > 0) {
            queryString.append(" WHERE");
        }
        for (String key: caseList) {
            queryString.append(" ");
            queryString.append(key);
            queryString.append(" AND");
        }
        if (queryString.substring(queryString.length() - 4).equals(" AND")) {
            queryString.delete(queryString.length() - 4, queryString.length());
        }
        return query(db, queryString.toString());
    }

    public static int update(SQLiteDatabase db, String tableName, String whereClause, ContentValues values) {
        Log.d("DBUpdate","size: " + values.size() );
        return db.update(tableName,  values, whereClause, null);
    }

    /**
     *
     * @param tableName
     * @param sqLiteDatabase
     */

/**
    public static void dropTable(String tableName, SQLiteDatabase sqLiteDatabase){
        String dropTableQuery = "DROP TABLE IF EXISTS '" + tableName + "';";
        sqLiteDatabase.execSQL(dropTableQuery);
    }

    /**
     *
     * @param tableName Name of the table
     * @param selection Column name to be selected
     * @param selectionArguments
     * @param sqLiteDatabase SQLiteDatabase Instance
     * @return Number of rows that were deleted
     */
/**
    public static int deleteRecordsWithCondition(String tableName, String selection, String[] selectionArguments, SQLiteDatabase sqLiteDatabase){
        int deletedRows = sqLiteDatabase.delete(tableName,selection + " LIKE ?",selectionArguments);
        return deletedRows;
    }

    /**
     * Deletes all rows from a table
     * @param tableName Name of the table
     * @param sqLiteDatabase SQLiteDatabase Instance
     * @return Number of rows that were deleted
     */

    /**
    public static int deleteAllRecords(String tableName, SQLiteDatabase sqLiteDatabase ){
        int deletedRows = 0;

        try {
            deletedRows = sqLiteDatabase.delete(tableName, "1", null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return deletedRows;
    }
}
     */
