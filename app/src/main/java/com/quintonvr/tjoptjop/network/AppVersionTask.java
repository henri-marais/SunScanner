package com.quintonvr.tjoptjop.network;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.content.pm.PackageManager;
import android.net.Uri;
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
import static com.quintonvr.tjoptjop.helpers.Utils.VITAL_PREFS;

public class AppVersionTask extends AsyncTask<Void, Void, Integer> {

    private Context mContext;


    public AppVersionTask(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        try {
            SharedPreferences prefs = mContext.getSharedPreferences(VITAL_PREFS, Context.MODE_PRIVATE);
            String customerCode = prefs.getString(PREFS_CUSTOMER_CODE, "");
            String activationToken = prefs.getString(PREFS_ACTIVATION_TOKEN, "");
            int appVersion = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
            String urlAddress = mContext.getString(R.string.domain_name) + mContext.getString(R.string.endpoint_appVersion);

            URL url = new URL(urlAddress);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
            os.write("versionCode="+appVersion);
            os.flush();
            os.close();

            Log.i("STATUS", String.valueOf(conn.getResponseCode()));
            Log.i("MSG" , conn.getResponseMessage());

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                //TODO: show server error message
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

            return Integer.valueOf(response);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    @Override
    protected void onPostExecute(Integer versionCode) {
        super.onPostExecute(versionCode);
        try {
            int appVersion = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
            if (versionCode > appVersion) {
                new AlertDialog.Builder(mContext)
                        .setTitle("Update Available")
                        .setMessage("Please update your app.")
                        .setPositiveButton(R.string.button_update, ((dialogInterface, i) -> {
                            String url = mContext.getString(R.string.domain_name) + mContext.getString(R.string.apk_download_path);
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            mContext.startActivity(browserIntent);
                            ((Activity)mContext).finishAffinity();
                        }))
                        .setNegativeButton(R.string.button_later, ((dialogInterface, i) -> {
                            dialogInterface.dismiss();
                        }))
                        .setCancelable(false)
                        .show();
            }
            new DatabaseSyncTask(mContext).execute();

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }
}
