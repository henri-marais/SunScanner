package com.quintonvr.tjoptjop.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.nfc.Tag;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHELP";
    private static final String DATABASE_NAME = "/user-records.db";
    public static final String TABLE_NAME = "records";
    public static final String TABLE_COLUMN_CUSTOMERCODE = "customerCode";
    public static final String TABLE_COLUMN_ACTIVATIONTOKEN = "activationToken";
    public static final String TABLE_COLUMN_TID = "tid";
    public static final String TABLE_COLUMN_TTEMP = "ttemp";
    public static final String TABLE_COLUMN_TTEMPMETA = "ttempmeta";
    public static final String TABLE_COLUMN_Q0 = "q0";
    public static final String TABLE_COLUMN_Q1 = "q1";
    public static final String TABLE_COLUMN_Q2 = "q2";
    public static final String TABLE_COLUMN_Q3 = "q3";
    public static final String TABLE_COLUMN_Q4 = "q4";
    public static final String TABLE_COLUMN_Q5 = "q5";
    public static final String TABLE_COLUMN_Q6 = "q6";
    public static final String TABLE_COLUMN_Q7 = "q7";
    public static final String TABLE_COLUMN_Q8 = "q8";
    public static final String TABLE_COLUMN_Q9 = "q9";
    public static final String TABLE_COLUMN_Q10 = "q10";
    public static final String TABLE_COLUMN_Q11 = "q11";
    public static final String TABLE_COLUMN_Q12 = "q12";
    public static final String TABLE_COLUMN_Q13 = "q13";
    public static final String TABLE_COLUMN_Q14 = "q14";
    public static final String TABLE_COLUMN_Q15 = "q15";
    public static final String TABLE_COLUMN_TDIR = "tdir";
    public static final String TABLE_COLUMN_RMETA = "rmeta";
    public static final String TABLE_COLUMN_TIMESTAMP = "timestamp";
    public static final int DATABASE_VERSION = 4;
    private Context mContext;

    public DatabaseHelper(Context context) {
        super(context, context.getFilesDir()+DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS records");
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS records(id INTEGER PRIMARY KEY, customerCode VARCHAR, activationToken VARCHAR, tid VARCHAR, " +
                    "ttemp VARCHAR, ttempmeta VARCHAR, q0 VARCHAR,q1 VARCHAR, q2 VARCHAR, q3 VARCHAR, q4 VARCHAR, q5 VARCHAR, q6 VARCHAR, q7 VARCHAR, " +
                    "q8 VARCHAR, q9 VARCHAR, q10 VARCHAR, q11 VARCHAR, q12 VARCHAR, q13 VARCHAR, q14 VARCHAR,q15 VARCHAR," +
                    "tdir VARCHAR, rmeta VARCHAR, timestamp DATETIME DEFAULT (datetime('now','localtime')));");
        }catch(SQLException e){
            Log.e("SQLDB",e.toString());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4){
           db.execSQL("ALTER TABLE records ADD COLUMN q0 VARCHAR DEFAULT NULL");
        }
    }

    public boolean insertNewValue(String CustomerCode, String activationToken, String tid, String ttemp, String ttempmeta,String q0, String q1, String q2,String q3,String q4,String q5,
                                    String q6,String q7,String q8,String q9,String q10,String q11,String q12,String q13,String q14,String q15,String tdir, String rmeta) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_COLUMN_CUSTOMERCODE, CustomerCode);
        contentValues.put(TABLE_COLUMN_ACTIVATIONTOKEN, activationToken);
        contentValues.put(TABLE_COLUMN_TID, tid);
        contentValues.put(TABLE_COLUMN_TTEMP, ttemp);
        contentValues.put(TABLE_COLUMN_TTEMPMETA, ttempmeta);
        contentValues.put(TABLE_COLUMN_Q0, q0);
        contentValues.put(TABLE_COLUMN_Q1, q1);
        contentValues.put(TABLE_COLUMN_Q2, q2);
        contentValues.put(TABLE_COLUMN_Q3, q3);
        contentValues.put(TABLE_COLUMN_Q4, q4);
        contentValues.put(TABLE_COLUMN_Q5, q5);
        contentValues.put(TABLE_COLUMN_Q6, q6);
        contentValues.put(TABLE_COLUMN_Q7, q7);
        contentValues.put(TABLE_COLUMN_Q8, q8);
        contentValues.put(TABLE_COLUMN_Q9, q9);
        contentValues.put(TABLE_COLUMN_Q10, q10);
        contentValues.put(TABLE_COLUMN_Q11, q11);
        contentValues.put(TABLE_COLUMN_Q12, q12);
        contentValues.put(TABLE_COLUMN_Q13, q13);
        contentValues.put(TABLE_COLUMN_Q14, q14);
        contentValues.put(TABLE_COLUMN_Q15, q15);
        contentValues.put(TABLE_COLUMN_TDIR,tdir);
        contentValues.put(TABLE_COLUMN_RMETA,rmeta);
        try {
            db.insertOrThrow(TABLE_NAME, null, contentValues);
            return true;
        }catch(SQLException e){
            Log.i("SQLDB",e.toString());
            return false;
        }
    }

    public Cursor getAllEntries(String table) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor resultSet = db.rawQuery("SELECT * FROM " + table,null);
        return resultSet;
    }

    public Cursor getSyncBundle(String table,int bundleSize){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor resultSet = db.rawQuery("SELECT * FROM " + TABLE_NAME + " LIMIT " + Integer.toString(bundleSize),null);
        return resultSet;
    }

    public boolean deleteAllEntries(String table) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(table, null, null);
        return true;
    }

    public boolean deleteSyncBundle(String table,int bundleSize){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            //This query deletes the fisrt bundleSize number of records from the table.
            db.delete(TABLE_NAME,"id IN (SELECT id FROM "+ TABLE_NAME + " LIMIT " + Integer.toString(bundleSize)+")",null);
            return true;
        }catch(SQLException e){
            Log.i("SQLDB",e.toString());
            return false;
        }
    }

    public long getNumRecords() {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, TABLE_NAME);
        db.close();
        return count;
    }

    public void dumpToFile() throws IOException {
        Writer output = null;
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), TABLE_NAME+".json");
        output = new BufferedWriter(new FileWriter(file));
        SQLiteDatabase db = this.getReadableDatabase();
        Log.d(TAG,"Exporting DB of "+ Long.toString(getNumRecords()));
        Cursor recordResultSet = getAllEntries(TABLE_NAME);
        recordResultSet.moveToFirst();
        try {
            for (int row = 0; row < recordResultSet.getCount(); row++) {
                String customerCode = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_CUSTOMERCODE));
                String activationToken = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_ACTIVATIONTOKEN));
                String tid = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_TID));
                String ttemp = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_TTEMP));
                String ttempmeta = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_TTEMPMETA));
                String q0 = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_Q0));
                String q1 = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_Q1));
                String q2 = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_Q2));
                String q3 = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_Q3));
                String q4 = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_Q4));
                String q5 = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_Q5));
                String q6 = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_Q6));
                String q7 = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_Q7));
                String q8 = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_Q8));
                String q9 = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_Q9));
                String q10 = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_Q10));
                String q11 = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_Q11));
                String q12 = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_Q12));
                String q13 = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_Q13));
                String q14 = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_Q14));
                String q15 = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_Q15));
                String dir = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_TDIR));
                String rmeta = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_RMETA));
                String timestamp = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_TIMESTAMP));

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("cust", customerCode);
                jsonParam.put("token", activationToken);
                jsonParam.put("tid", tid);
                jsonParam.put("ttemp", ttemp);
                jsonParam.put("ttempmeta", ttempmeta);
                jsonParam.put("q0", q0);
                jsonParam.put("q1", q1);
                jsonParam.put("q2", q2);
                jsonParam.put("q3", q3);
                jsonParam.put("q4", q4);
                jsonParam.put("q5", q5);
                jsonParam.put("q6", q6);
                jsonParam.put("q7", q7);
                jsonParam.put("q8", q8);
                jsonParam.put("q9", q9);
                jsonParam.put("q10", q10);
                jsonParam.put("q11", q11);
                jsonParam.put("q12", q12);
                jsonParam.put("q13", q13);
                jsonParam.put("q14", q14);
                jsonParam.put("q15", q15);
                jsonParam.put("dir", dir);
                jsonParam.put("rmeta", rmeta);
                jsonParam.put("timestamp", timestamp);
                Log.d(TAG, "Build record " + Integer.toString(row));
                output.write(jsonParam.toString());
                Log.d(TAG, "Wrote record to file.");
                recordResultSet.moveToNext();
            }
        }catch(Exception e){
            Log.d(TAG,e.toString());
        }
        output.close();
    }
}
