package com.quintonvr.tjoptjop.network;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.quintonvr.tjoptjop.R;
import com.quintonvr.tjoptjop.helpers.DatabaseHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.ErrorManager;

import static com.quintonvr.tjoptjop.helpers.DatabaseHelper.TABLE_COLUMN_TIMESTAMP;
import static com.quintonvr.tjoptjop.helpers.DatabaseHelper.TABLE_COLUMN_RMETA;
import static com.quintonvr.tjoptjop.helpers.DatabaseHelper.TABLE_COLUMN_TDIR;
import static com.quintonvr.tjoptjop.helpers.DatabaseHelper.TABLE_COLUMN_Q15;
import static com.quintonvr.tjoptjop.helpers.DatabaseHelper.TABLE_COLUMN_Q14;
import static com.quintonvr.tjoptjop.helpers.DatabaseHelper.TABLE_COLUMN_Q13;
import static com.quintonvr.tjoptjop.helpers.DatabaseHelper.TABLE_COLUMN_Q12;
import static com.quintonvr.tjoptjop.helpers.DatabaseHelper.TABLE_COLUMN_Q11;
import static com.quintonvr.tjoptjop.helpers.DatabaseHelper.TABLE_COLUMN_Q10;
import static com.quintonvr.tjoptjop.helpers.DatabaseHelper.TABLE_COLUMN_Q9;
import static com.quintonvr.tjoptjop.helpers.DatabaseHelper.TABLE_COLUMN_Q8;
import static com.quintonvr.tjoptjop.helpers.DatabaseHelper.TABLE_COLUMN_Q7;
import static com.quintonvr.tjoptjop.helpers.DatabaseHelper.TABLE_COLUMN_Q6;
import static com.quintonvr.tjoptjop.helpers.DatabaseHelper.TABLE_COLUMN_Q5;
import static com.quintonvr.tjoptjop.helpers.DatabaseHelper.TABLE_COLUMN_Q4;
import static com.quintonvr.tjoptjop.helpers.DatabaseHelper.TABLE_COLUMN_Q3;
import static com.quintonvr.tjoptjop.helpers.DatabaseHelper.TABLE_COLUMN_Q2;
import static com.quintonvr.tjoptjop.helpers.DatabaseHelper.TABLE_COLUMN_Q1;
import static com.quintonvr.tjoptjop.helpers.DatabaseHelper.TABLE_COLUMN_Q0;
import static com.quintonvr.tjoptjop.helpers.DatabaseHelper.TABLE_COLUMN_TTEMPMETA;
import static com.quintonvr.tjoptjop.helpers.DatabaseHelper.TABLE_COLUMN_TTEMP;
import static com.quintonvr.tjoptjop.helpers.DatabaseHelper.TABLE_COLUMN_TID;
import static com.quintonvr.tjoptjop.helpers.DatabaseHelper.TABLE_COLUMN_ACTIVATIONTOKEN;
import static com.quintonvr.tjoptjop.helpers.DatabaseHelper.TABLE_COLUMN_CUSTOMERCODE;

import static com.quintonvr.tjoptjop.helpers.DatabaseHelper.TABLE_NAME;
import static com.quintonvr.tjoptjop.helpers.Utils.PREFS_ACTIVATION_TOKEN;
import static com.quintonvr.tjoptjop.helpers.Utils.PREFS_CUSTOMER_CODE;
import static com.quintonvr.tjoptjop.helpers.Utils.VITAL_PREFS;

public class DatabaseSyncTask extends AsyncTask<Void, Void, Integer> {
    private static final int DB_EMPTY = 100;
    private static final int DB_SYNC_SUCCESSFUL = 101;
    private static final int DB_SYNC_ERROR = 102;
    private static final int DB_AUTH_ERROR = 103;
    private static String errorMsg="";
    private String update_msg = "";
    private static final int BUNDLE_SIZE = 50;
    private static final int MAX_RETRIES = 3;
    private static final int MAX_RETRY_TIMEOUT = 5000;
    private SyncInterface mSyncInterface;

    private Context mContext;
    private DatabaseHelper mydb;
    long numRecords;
    int numBundles,currentBundle;
    boolean fatal_error = false;
    boolean sync_complete = false;


    public DatabaseSyncTask(Context context) {
        mContext = context;
    }

    public void setListener(SyncInterface listener){
        mSyncInterface = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(Void... voids) {
            SharedPreferences prefs = mContext.getSharedPreferences(VITAL_PREFS, Context.MODE_PRIVATE);
            String mCustomerCode = prefs.getString(PREFS_CUSTOMER_CODE,"");
            String mActivationToken = prefs.getString(PREFS_ACTIVATION_TOKEN,"");
            String urlAddress = mContext.getString(R.string.domain_name) + mContext.getString(R.string.endpoint_synchronisation);
            mydb = new DatabaseHelper(mContext);
            numRecords = mydb.getNumRecords();
            numBundles = (int) (numRecords / BUNDLE_SIZE);
            if ((numRecords % BUNDLE_SIZE) != 0){
                numBundles++;
            }
            if (numBundles > 0) {
                do {
                    currentBundle = 0;
                    do{
                        Cursor recordResultSet = mydb.getSyncBundle(TABLE_NAME, BUNDLE_SIZE);
                        recordResultSet.moveToFirst();
                        try {
                            JSONArray jsonArray = new JSONArray();
                            update_msg = "Building packet "+Integer.toString(currentBundle+1);
                            publishProgress();
                            for (int row = 0; row < recordResultSet.getCount(); row++) {
                                String customerCode = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_CUSTOMERCODE));
                                String activationToken = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_ACTIVATIONTOKEN));
                                String tid = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_TID));
                                String ttemp = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_TTEMP));
                                String ttempmeta = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_TTEMPMETA));
                                String q0 = "";
                                try {
                                    q0 = recordResultSet.getString(recordResultSet.getColumnIndex(TABLE_COLUMN_Q0));
                                }catch(Exception e){
                                    q0="";
                                }
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
                                jsonArray.put(jsonParam);
                                recordResultSet.moveToNext();
                            }

                            int retries = 1;
                            boolean packetSent = false;
                            do{
                                try {
                                    update_msg = "Sending packet to server...";
                                    publishProgress();
                                    packetSent = sendPacket(urlAddress,jsonArray);
                                }catch(Exception e) {
                                    e.printStackTrace();
                                    update_msg = e.toString();
                                    fatal_error = true;
                                }
                                if (!packetSent){
                                    retries++;
                                    update_msg="Error sync'ing bundle "+Integer.toString(currentBundle)+": "+ errorMsg;
                                    publishProgress();
                                    try{
                                        Random rand = new Random();
                                        Thread.sleep(rand.nextInt(MAX_RETRY_TIMEOUT));
                                    }catch(Exception e){
                                        e.printStackTrace();
                                        update_msg = e.toString();
                                        fatal_error = true;
                                    }
                                }else{
                                    currentBundle++;
                                    mydb.deleteSyncBundle(TABLE_NAME, BUNDLE_SIZE);
                                    mydb.close();
                                    publishProgress();
                                }
                                if (retries > MAX_RETRIES){
                                    update_msg = "Maximum number of retries exceeded. Fatal error!";
                                    publishProgress();
                                    fatal_error = true;
                                }
                            }while(!packetSent && !fatal_error);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }while((currentBundle < numBundles) && !fatal_error);
                    if (currentBundle >= numBundles){sync_complete=true;}
                } while ((!fatal_error) && (!sync_complete));
                mydb.close();
                if (fatal_error){
                    return DB_SYNC_ERROR;
                }else{
                    return  DB_SYNC_SUCCESSFUL;
                }
            }else{
                return DB_EMPTY;
            }
    }

    protected void onProgressUpdate(Void...voids){
       int progress =  (int) ( (float)currentBundle / (float)numBundles * 100);
       if (update_msg.isEmpty()){
           mSyncInterface.SyncProgressUpdate(progress,"Successfully syncronised packet "+Integer.toString(currentBundle+1)+" of "+Integer.toString(numBundles));
       }else{
           mSyncInterface.SyncProgressUpdate(progress,update_msg);
           update_msg="";
       }
    }

    @Override
    protected void onPostExecute(Integer code) {
        super.onPostExecute(code);
        if (code != DB_AUTH_ERROR){ mydb.close();}
        switch (code) {
            case DB_SYNC_SUCCESSFUL: {
                break;
            }
            case DB_EMPTY: {
                mSyncInterface.SyncProgressUpdate(-1,"No records to synchronise. Everything is up to date.");
                break;
            }
            case DB_SYNC_ERROR: {
                mSyncInterface.SyncProgressUpdate(-2,"A persistent error has occured during synchronisation. Please try again later.");
                break;
            }
            case DB_AUTH_ERROR: {
                mSyncInterface.SyncProgressUpdate(-3,"The token associated with this device is not valid.");
                break;
            }
        }
    }

    private boolean sendPacket(String destination,JSONArray input){
        try {
            URL url = new URL(destination);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            DataOutputStream os_post = new DataOutputStream(conn.getOutputStream());
            os_post.writeBytes(input.toString());
            os_post.flush();
            os_post.close();
            Log.i("SYNC_STATUS", String.valueOf(conn.getResponseCode()));
            Log.i("SYNC_MSG", conn.getResponseMessage());
            InputStream is = conn.getInputStream();
            StringBuffer sb = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            is.close();
            String response = sb.toString();
            response = response.trim();
            conn.disconnect();

            if ((conn.getResponseCode() == HttpURLConnection.HTTP_OK) & response.equals("success")) {
                return true;
            } else {
                errorMsg = response;
                return false;
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}


