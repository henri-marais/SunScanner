package com.quintonvr.tjoptjop.network;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.quintonvr.tjoptjop.R;
import com.quintonvr.tjoptjop.activities.MainActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.quintonvr.tjoptjop.helpers.Utils.PREFS_ACTIVATION_TOKEN;
import static com.quintonvr.tjoptjop.helpers.Utils.PREFS_CUSTOMER_CODE;
import static com.quintonvr.tjoptjop.helpers.Utils.PREFS_CUSTOMER_CODE;
import static com.quintonvr.tjoptjop.helpers.Utils.PREFS_QUESTIONS_ID;
import static com.quintonvr.tjoptjop.helpers.Utils.VITAL_PREFS;

public class LoginTask extends AsyncTask<Void, Void, Boolean> {
    Context mContext;
    String mCustomerCode, mActivationToken;
    ProgressDialog progressDialog;

    public LoginTask(Context context, String institution, String activationToken) {
        mContext = context;
        mCustomerCode = institution;
        mActivationToken = activationToken;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = ProgressDialog.show(mContext, "Authenticating...", "Please wait");  //show a progress dialog
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            String urlAddress = mContext.getString(R.string.domain_name) + "/app/verify.php";

            URL url = new URL(urlAddress);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
            os.write("customerCode="+ mCustomerCode + "&activationToken="+mActivationToken);
            os.flush();
            os.close();

            Log.i("TjopTjop-Login-STS", String.valueOf(conn.getResponseCode()));
            Log.i("TjopTjop-Login-MSG", conn.getResponseMessage());

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                //TODO: show server error message
                return false;
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

            String response = sb.toString();
            conn.disconnect();
            if (response.equals("1")) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        progressDialog.dismiss();

        if (success) {
            SharedPreferences prefs = mContext.getSharedPreferences(VITAL_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PREFS_CUSTOMER_CODE, mCustomerCode);
            editor.putString(PREFS_ACTIVATION_TOKEN, mActivationToken);
            editor.commit();
            Intent intent = new Intent(mContext, MainActivity.class);
            mContext.startActivity(intent);
            ((Activity)mContext).finish();
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(mContext)
                    .setTitle("Please try again")
                    .setMessage("The details provided are incorrect.")
                    .setPositiveButton(R.string.button_ok, ((dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    }))
                    .setCancelable(false);
            dialog.show();
        }
    }
}
