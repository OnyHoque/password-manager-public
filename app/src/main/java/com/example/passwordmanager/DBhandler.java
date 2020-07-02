package com.example.passwordmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.LinkedList;


public class DBhandler extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "user_db";
    private static final String TABLE_Users = "user_data";
    private static final String KEY_ID = "id";
    private static final String KEY_ACCOUNT = "data1";
    private static final String KEY_USERNAME = "data2";
    private static final String KEY_PASSWORD = "data3";

    Context context;

    public DBhandler(Context context){
        super(context,DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_Users + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_ACCOUNT + " TEXT,"
                + KEY_USERNAME + " TEXT,"
                + KEY_PASSWORD + " TEXT"+ ")";
        db.execSQL(CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_Users);
        onCreate(db);
    }

    public long insertData(String account_name, String username, String password){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cValues = new ContentValues();
        cValues.put(KEY_ACCOUNT, account_name);
        cValues.put(KEY_USERNAME, username);
        cValues.put(KEY_PASSWORD, password);
        long newRowId = db.insert(TABLE_Users,null, cValues);
        db.close();
        return newRowId;
    }


    public String getData()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] columns = {KEY_ID, KEY_USERNAME, KEY_PASSWORD, KEY_ACCOUNT};
        Cursor cursor =db.query(TABLE_Users,columns,null,null,null,null,null);
        StringBuffer buffer= new StringBuffer();
        while (cursor.moveToNext())
        {
            int cid =cursor.getInt(cursor.getColumnIndex(KEY_ID));
            String  account_name =cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT));
            String name =cursor.getString(cursor.getColumnIndex(KEY_USERNAME));
            String  password =cursor.getString(cursor.getColumnIndex(KEY_PASSWORD));
            buffer.append(cid + "," + account_name + "," + name + "," + password + ";;;");
        }
        return buffer.toString();
    }

    public LinkedList<user_account> getData_raw()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] columns = {KEY_ID, KEY_USERNAME, KEY_PASSWORD, KEY_ACCOUNT};
        Cursor cursor =db.query(TABLE_Users,columns,null,null,null,null,null);
        StringBuffer buffer= new StringBuffer();
        LinkedList<user_account> acc_list = new LinkedList<user_account>();
        while (cursor.moveToNext())
        {
            String  account_name =cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT));
            String name =cursor.getString(cursor.getColumnIndex(KEY_USERNAME));
            String  password =cursor.getString(cursor.getColumnIndex(KEY_PASSWORD));
            user_account ua = new user_account(account_name, name, password);
            acc_list.add(ua);
        }
        return acc_list;
    }


    public ArrayList<user_account> getData_class()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] columns = {KEY_ID, KEY_USERNAME, KEY_PASSWORD};
        Cursor cursor =db.query(TABLE_Users,columns,null,null,null,null,null);
        StringBuffer buffer= new StringBuffer();

        ArrayList<user_account> ua_list = new ArrayList<>();

        while (cursor.moveToNext())
        {
            String account_name = cursor.getString(cursor.getColumnIndex(KEY_ACCOUNT));
            String name = cursor.getString(cursor.getColumnIndex(KEY_USERNAME));
            String  password =cursor.getString(cursor.getColumnIndex(KEY_PASSWORD));
            user_account ua = new user_account(account_name, name, password);
            ua_list.add(ua);
        }
        return ua_list;
    }



    public void clearTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_Users);
        String CREATE_TABLE = "CREATE TABLE " + TABLE_Users + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_ACCOUNT + " TEXT,"
                + KEY_USERNAME + " TEXT,"
                + KEY_PASSWORD + " TEXT"+ ")";
        db.execSQL(CREATE_TABLE);
    }

    public void deleteRow(String data2, String data3, String data4){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_Users,
                KEY_ACCOUNT + "='"+data2+"' AND " + KEY_USERNAME + "='"+data3+"' AND " +
                        KEY_PASSWORD + "='"+data4+"'",null);
    }


    public void updateRow(String data2, String data3, String data4, String data5, String data6, String data7) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "UPDATE "+TABLE_Users+" SET "+KEY_ACCOUNT+"='"+data5+"', "+KEY_USERNAME+"='"+data6+"', "+KEY_PASSWORD+"='"+data7+"' WHERE "+KEY_ACCOUNT+ "='" +data2+"' AND "+KEY_USERNAME+"='"+data3+"' AND "+KEY_PASSWORD+"='"+data4+"'";
        db.execSQL(sql);
    }
}
