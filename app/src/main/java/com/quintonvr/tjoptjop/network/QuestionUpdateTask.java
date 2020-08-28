package com.quintonvr.tjoptjop.network;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
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
import static com.quintonvr.tjoptjop.helpers.Utils.PREFS_CUSTOMER_CODE;
import static com.quintonvr.tjoptjop.helpers.Utils.PREFS_QUESTIONS;
import static com.quintonvr.tjoptjop.helpers.Utils.PREFS_QUESTIONS_ID;
import static com.quintonvr.tjoptjop.helpers.Utils.VITAL_PREFS;

public class QuestionUpdateTask extends AsyncTask<Void, Void, Integer> {
    public static String TAG = "QuestionUpdater";
    private ProgressDialog progressDialog;

    private Context mContext;

    public QuestionUpdateTask(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = ProgressDialog.show(mContext, "Updating the active question set...", "Please wait");  //show a progress dialog
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        try {
            SharedPreferences prefs = mContext.getSharedPreferences(VITAL_PREFS, Context.MODE_PRIVATE);
            String customerCode = prefs.getString(PREFS_CUSTOMER_CODE, "");
            int questionSetID = prefs.getInt(PREFS_QUESTIONS_ID,-1);
            String urlAddress = mContext.getString(R.string.domain_name) + mContext.getString(R.string.endpoint_questionUpdate);

            URL url = new URL(urlAddress);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
            os.write("qid="+questionSetID+"&customerCode="+customerCode);
            os.flush();
            os.close();

            Log.i(TAG, String.valueOf(conn.getResponseCode()));
            Log.i(TAG , conn.getResponseMessage());

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e(TAG,"Error from server"+conn.getResponseMessage());
            }

            InputStream is = conn.getInputStream();
            StringBuffer sb = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = "";
            while( (line = reader.readLine()) != null){
                sb.append(line);
            }
            reader.close();
            is.close();
            conn.disconnect();
            /*Deal with the server response*/
            JSONObject response = new JSONObject(sb.toString());
            if (response.getString("status").equals("uptodate")){
                return 204; //Standard response for no content
            }else if (response.getString("status").equals("new")){
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(PREFS_QUESTIONS_ID, response.getInt("qid"));
                editor.putString(PREFS_QUESTIONS,response.getString("questions"));
                editor.commit();
                return 200; //OK, signalling that questions were updated
            }else{
                return 400; //Otherwise an error has occurred.
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 400;
    }

    @Override
    protected void onPostExecute(Integer responseCode) {
        super.onPostExecute(responseCode);
        progressDialog.dismiss();
        try {
            if (responseCode == 200) {
                SharedPreferences prefs = mContext.getSharedPreferences(VITAL_PREFS, Context.MODE_PRIVATE);
                int questionSetID = prefs.getInt(PREFS_QUESTIONS_ID,-1);
                new AlertDialog.Builder(mContext)
                        .setTitle("Questions Updated")
                        .setMessage("A new question set was retrieved! (id: "+Integer.toString(questionSetID)+")")
                        .setPositiveButton(R.string.button_ok, ((dialogInterface, i) -> {
                            dialogInterface.dismiss();
                        }))
                        .setCancelable(false)
                        .show();
            }
        }catch(Exception e){
            Log.e(TAG,"Error has occured during question update");
        }

    }
}



