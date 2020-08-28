package com.quintonvr.tjoptjop.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.quintonvr.tjoptjop.R;
import com.quintonvr.tjoptjop.network.QuestionUpdateTask;

import static com.quintonvr.tjoptjop.helpers.Utils.PREFS_ACTIVATION_TOKEN;
import static com.quintonvr.tjoptjop.helpers.Utils.PREFS_CUSTOMER_CODE;
import static com.quintonvr.tjoptjop.helpers.Utils.PREFS_QUESTIONS_ID;
import static com.quintonvr.tjoptjop.helpers.Utils.VITAL_PREFS;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView appVersion = findViewById(R.id.app_version);
        try {
            appVersion.setText(this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences(VITAL_PREFS, Context.MODE_PRIVATE);
        String institution = prefs.getString(PREFS_CUSTOMER_CODE, "");
        String activationToken = prefs.getString(PREFS_ACTIVATION_TOKEN, "");

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!institution.equals("") && !activationToken.equals("")) {
                    Intent menuIntent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(menuIntent);
                    finish();
                } else {
                    Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(loginIntent);
                    finish();
                }
            }
        }, 2000);
    }
}
