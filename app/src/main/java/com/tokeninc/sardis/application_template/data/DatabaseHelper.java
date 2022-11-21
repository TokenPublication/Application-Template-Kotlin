package com.tokeninc.sardis.application_template.data;
/*
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tokeninc.sardis.application_template.Helpers.PrintHelpers.DateUtil;
import com.tokeninc.sardis.application_template.Helpers.PrintHelpers.PrintHelper;
import com.tokeninc.sardis.application_template.Helpers.PrintHelpers.PrintServiceBinding;

import java.util.ArrayList;
import java.util.List;


public class DatabaseHelper extends SQLiteOpenHelper {

    public PrintServiceBinding printService;

    public static String DATABASE = "database.db";

    public static String SALE_TABLE ="sale_table";
    public static String CARD_NO ="card_no";
    public static String AMOUNT ="sale_amount";
    public static String PROCESS_TIME ="process_time";

    public static String ACT_TABLE ="act_table";
    public static String MERCHANT_ID ="merchant_id";
    public static String TERMINAL_ID ="terminal_id";
    public static String IP ="ip_no";
    public static String PORT ="port_no";

    public static String TX_INFO ="tx_info";
    public static String BATCH = "batch_no";
    public static String TX = "tx_no";
    public static String SALE_ID = "sale_id";

    String sale_db, act_db, tx_info_table;

    String process_time, batch_no, tx_no, sale_id;

    public DatabaseHelper(Context context) {
        super(context, DATABASE, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        sale_db = "CREATE TABLE IF NOT EXISTS "+ SALE_TABLE +"("+CARD_NO + " Text, "+AMOUNT + " Text, "+PROCESS_TIME + " Text, "+TX+ " Text, "+ SALE_ID + " Text, "+BATCH+ " Text);";
        db.execSQL(sale_db);

        act_db = "CREATE TABLE IF NOT EXISTS "+ACT_TABLE+"("+MERCHANT_ID + " Text, "+TERMINAL_ID + " Text, "+IP + " Text, "+PORT + " Text);";
        db.execSQL(act_db);

        tx_info_table = "CREATE TABLE IF NOT EXISTS "+TX_INFO+"("+BATCH + " Text, "+TX +" Text, "+ SALE_ID +" Text) ;";
        db.execSQL(tx_info_table);

        CheckTxInfoTableIsEmpty();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ SALE_TABLE +" ;");
    }

    public void insertSaleData(String card_no, String sale_amount, String process_time , String tx_no, String sale_id, String batch_no){
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues= new ContentValues();

            contentValues.put(CARD_NO, card_no);
            contentValues.put(AMOUNT,sale_amount);
            contentValues.put(PROCESS_TIME, process_time);
            contentValues.put(TX,tx_no);
            contentValues.put(SALE_ID,sale_id);
            contentValues.put(BATCH,batch_no);
            db.insert(SALE_TABLE,null,contentValues);
            db.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public List<com.tokeninc.sardis.application_template.Helpers.DataBase.DataModel> getData(){
        List<com.tokeninc.sardis.application_template.Helpers.DataBase.DataModel> data=new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from "+ SALE_TABLE +" ;",null);
        StringBuffer stringBuffer = new StringBuffer();
        com.tokeninc.sardis.application_template.Helpers.DataBase.DataModel dataModel = null;
        while (cursor.moveToNext()) {
            dataModel= new com.tokeninc.sardis.application_template.Helpers.DataBase.DataModel();
            String card_no = cursor.getString(cursor.getColumnIndexOrThrow("card_no"));
            String sale_amount = cursor.getString(cursor.getColumnIndexOrThrow("sale_amount"));
            String process_time = cursor.getString(cursor.getColumnIndexOrThrow("process_time"));
            String tx_no = cursor.getString(cursor.getColumnIndexOrThrow("tx_no"));
            String sale_id = cursor.getString(cursor.getColumnIndexOrThrow("sale_id"));
            dataModel.setCard_no(card_no);
            dataModel.setSale_amount(sale_amount);
            dataModel.setProcess_time(process_time);
            dataModel.setApproval_code(tx_no);
            dataModel.setSerial_no(sale_id);
            stringBuffer.append(dataModel);
            data.add(dataModel);
        }
        cursor.close();
        db.close();
        return data;
    }

    public String query(String queryStr) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(queryStr, null);
        String
                result = null;
        if (cursor.moveToFirst()) {
            result = cursor.getString(0);
        }
        cursor.close();
        return result;
    }

    public static int update(SQLiteDatabase db, String tableName, String whereClause, ContentValues values) {
        return db.update(tableName,  values, whereClause, null);
    }

    public Integer getBatchNo() {
        String query = "SELECT " + BATCH + " FROM " + TX_INFO + " LIMIT 1";
        String batch_no = query(query);
        return batch_no == null ? null : Integer.valueOf(batch_no);
    }

    public void updateBatch(String batch_no){
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues v = new ContentValues();
            v.put(BATCH, batch_no);
            update(db, TX_INFO, null, v);
            db.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public Integer getTxNo() {
        String query = "SELECT " + TX + " FROM " + TX_INFO + " LIMIT 1";
        String tx_no = query(query);
        return tx_no == null ? null : Integer.valueOf(tx_no);
    }

    public void updateTxNo(String tx_no){
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues v = new ContentValues();
            v.put(TX, tx_no);
            update(db, TX_INFO, null, v);
            db.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public Integer getSaleID() {
        String query = "SELECT " + SALE_ID + " FROM " + TX_INFO + " LIMIT 1";
        String sale_count_no = query(query);
        return sale_count_no == null ? null : Integer.valueOf(sale_count_no);
    }

    public void updateSaleID(String sale_id_no){
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues v = new ContentValues();
            v.put(SALE_ID, sale_id_no);
            update(db, TX_INFO, null, v);
            db.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String getMerchantId() {
        String query = "SELECT " + MERCHANT_ID + " FROM " + ACT_TABLE + " LIMIT 1";
        return query(query);
    }
    public void updateMerchantId(String merchant_id){
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues v = new ContentValues();
            v.put(MERCHANT_ID, merchant_id);
            update(db, ACT_TABLE, null, v);
            db.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String getTerminalId() {
        String query = "SELECT " + TERMINAL_ID + " FROM " + ACT_TABLE + " LIMIT 1";
        return query(query);
    }
    public void updateTerminalId(String terminal_id){
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues v = new ContentValues();
            v.put(TERMINAL_ID, terminal_id);
            update(db, ACT_TABLE, null, v);
            db.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String getIP_NO() {
        String query = "SELECT " + IP + " FROM " + ACT_TABLE + " LIMIT 1";
        return query(query);
    }
    public void updateIP_NO(String ip_no){
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues v = new ContentValues();
            v.put(IP, ip_no);
            update(db, ACT_TABLE, null, v);
            db.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String getPort() {
        String query = "SELECT " + PORT + " FROM " + ACT_TABLE + " LIMIT 1";
        return query(query);
    }
    public void updatePort(String port_no){
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues v = new ContentValues();
            v.put(PORT, port_no);
            update(db, ACT_TABLE, null, v);
            db.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

public void batchClose(){
        printService = new PrintServiceBinding();
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS sale_table");
        createTables();

        String batchNo = String.valueOf(getBatchNo());

        int batchNo_int = Integer.parseInt(batchNo);
        String batchNo_str = String.valueOf(batchNo_int + 1);
        updateBatch(batchNo_str);

        String txNo = String.valueOf(getTxNo());

        String MID = getMerchantId();
        String TID = getTerminalId();

        printService.print(PrintHelper.PrintBatchClose(batchNo, txNo, MID, TID));

        updateTxNo("0");

        db.close();
    }

    public void SaveSaleToDB(String card_no, String sale_amount) {
        CheckTxInfoTableIsEmpty();

        SQLiteDatabase db = this.getWritableDatabase();

        String batchNo = String.valueOf(getBatchNo());
        batch_no = batchNo;

        String txNo = String.valueOf(getTxNo());
        int txNo_int = Integer.parseInt(txNo);
        String txNo_str = String.valueOf(txNo_int + 1);
        tx_no = txNo_str;
        updateTxNo(txNo_str);

        String saleID = String.valueOf(getSaleID());
        int saleID_int = Integer.parseInt(saleID);
        String saleID_str = String.valueOf(saleID_int + 1);
        sale_id = saleID_str;
        updateSaleID(saleID_str);

        // WRITE SALE DATA TO DATABASE
        process_time = DateUtil.getDate("dd-MM-yy");
        insertSaleData(card_no, sale_amount, process_time,  tx_no, sale_id, batch_no);
        db.close();
    }

    public void createTables(){

        SQLiteDatabase db = this.getWritableDatabase();

        sale_db = "CREATE TABLE IF NOT EXISTS "+ SALE_TABLE +"("+CARD_NO + " Text, "+AMOUNT + " Text, "+PROCESS_TIME + " Text, "+TX+ " Text, "+ SALE_ID + " Text, "+BATCH+ " Text);";
        db.execSQL(sale_db);

        act_db = "CREATE TABLE IF NOT EXISTS "+ACT_TABLE+"("+MERCHANT_ID + " Text, "+TERMINAL_ID + " Text);";
        db.execSQL(act_db);

        tx_info_table = "CREATE TABLE IF NOT EXISTS "+TX_INFO+"("+BATCH + " Text, "+TX +" Text, "+ SALE_ID +" Text) ;";
        db.execSQL(tx_info_table);

        CheckTxInfoTableIsEmpty();
    }

    public void CheckTxInfoTableIsEmpty(){

        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            ContentValues contentValues2 = new ContentValues();

            String batchNo = String.valueOf(getBatchNo());
            String txNo = String.valueOf(getTxNo());
            String saleID = String.valueOf(getSaleID());

            if ( batchNo.equals("") || batchNo.equals("null") && txNo.equals("") || txNo.equals("null") && saleID.equals("") || saleID.equals("null"))
            {
                contentValues.put(BATCH, "1");
                contentValues.put(TX, "0");
                contentValues.put(SALE_ID, "0");
                db.insert(TX_INFO,null,contentValues);
            }

            // No activation check in app temp. That's why MID, TID, IP and PORT information is filled in, when the application is loaded.
            String terminal_id = String.valueOf(getTerminalId());
            String merchant_id = String.valueOf(getMerchantId());
            String ip_no = String.valueOf(getIP_NO());
            String port_no = String.valueOf(getPort());

            if ( terminal_id.equals("") || terminal_id.equals("null") && merchant_id.equals("") || merchant_id.equals("null") && ip_no.equals("") || ip_no.equals("null") && port_no.equals("") || port_no.equals("null"))
            {
                contentValues2.put(TERMINAL_ID, "000005BB");
                contentValues2.put(MERCHANT_ID, "52487539624");
                contentValues2.put(IP, "192.168.1.1");
                contentValues2.put(PORT, "1040");
                db.insert(ACT_TABLE,null,contentValues2);
            }

            db.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public boolean CheckTableIsEmpty(){
        boolean empty = true;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.rawQuery("SELECT COUNT(*) FROM sale_table", null);
        if (cur != null && cur.moveToFirst()) {
            empty = (cur.getInt (0) == 0);
        }
        cur.close();
        db.close();
        return empty;
    }

    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }
}

 */
