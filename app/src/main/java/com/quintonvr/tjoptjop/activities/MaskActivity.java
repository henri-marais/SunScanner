package com.quintonvr.tjoptjop.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.quintonvr.tjoptjop.R;
import com.quintonvr.tjoptjop.helpers.DatabaseHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_ADDRESS;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_CITY;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_DIRECTION;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_EMERGENCY_CONTACT;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_IDTYPE;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_ID_NUMBER;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_MASK;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_NAME;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_TEMPERATURE;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_TEMPTYPE;
import static com.quintonvr.tjoptjop.helpers.Utils.PREFS_ACTIVATION_TOKEN;
import static com.quintonvr.tjoptjop.helpers.Utils.PREFS_CUSTOMER_CODE;
import static com.quintonvr.tjoptjop.helpers.Utils.VITAL_PREFS;

public class MaskActivity extends AppCompatActivity {

    private String mUserID, mTemperature;
    private String mIDtype,mTempType;
    private String mDir;
    private String mName;
    private String mPhone;
    private String mAddress;
    private String mCity;
    private Button mWearingMask, mIssueMask;
    private ImageView mCancel, mConfirm;
    private boolean isWearingMask, issuedMask = false;

    private String TAG = "MaskActivity";

    DatabaseHelper mydb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mask);

        mWearingMask = findViewById(R.id.wearing_mask_button);
        mIssueMask = findViewById(R.id.issue_mask_button);

        mydb = new DatabaseHelper(this);

        Intent intent = getIntent();
        if (intent != null) {
            mDir = intent.getStringExtra(INTENT_EXTRA_DIRECTION);
            mUserID = intent.getStringExtra(INTENT_EXTRA_ID_NUMBER);
            mIDtype = intent.getStringExtra(INTENT_EXTRA_IDTYPE);
            mTemperature = intent.getStringExtra(INTENT_EXTRA_TEMPERATURE);
            mTempType = intent.getStringExtra(INTENT_EXTRA_TEMPTYPE);
            mName= intent.getStringExtra(INTENT_EXTRA_NAME);
            mPhone = intent.getStringExtra(INTENT_EXTRA_EMERGENCY_CONTACT);
            mAddress = intent.getStringExtra(INTENT_EXTRA_ADDRESS);
            mCity = intent.getStringExtra(INTENT_EXTRA_CITY);
            if (mUserID != null) {
                Log.d(TAG, "USER ID: "+ mUserID);
                Log.d(TAG, "USER TEMPERATURE: "+ mTemperature);
            } else {
                Toast.makeText(this, "Select IDENTIFICATION first", Toast.LENGTH_LONG).show();
                finish();
            }

            mWearingMask.setOnClickListener(v -> {
                mWearingMask.setBackgroundColor(getResources().getColor(R.color.colorPrimaryHighlighted));
                int wearing = 1;
                mIssueMask.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                int issued = 0;
                if (mUserID != null && ((mTemperature != null) | (mDir.equals("OUT")))) {
                    Intent intentQuestions;
                    SharedPreferences userPrefs = this.getSharedPreferences("tjoptjopprefs",MODE_PRIVATE);
                    Boolean wellness_check = userPrefs.getBoolean("wellness_check",false);
                    if (wellness_check){
                        intentQuestions = new Intent(this, WellnessCheckActivity.class);
                    }else {
                        intentQuestions = new Intent(this, QuestionsActivity.class);
                    }
                    intentQuestions.putExtra(INTENT_EXTRA_ID_NUMBER, mUserID);
                    intentQuestions.putExtra(INTENT_EXTRA_IDTYPE, mIDtype);
                    intentQuestions.putExtra(INTENT_EXTRA_TEMPERATURE, mTemperature);
                    intentQuestions.putExtra(INTENT_EXTRA_TEMPTYPE, mTempType);
                    intentQuestions.putExtra(INTENT_EXTRA_DIRECTION,mDir);
                    intentQuestions.putExtra(INTENT_EXTRA_MASK, "Y");
                    if (mName != null) {
                        intentQuestions.putExtra(INTENT_EXTRA_NAME, mName);
                        intentQuestions.putExtra(INTENT_EXTRA_EMERGENCY_CONTACT, mPhone);
                        intentQuestions.putExtra(INTENT_EXTRA_ADDRESS, mAddress);
                        intentQuestions.putExtra(INTENT_EXTRA_CITY, mCity);
                    }
                    startActivity(intentQuestions);
                }
            });

            mIssueMask.setOnClickListener(v -> {
                mIssueMask.setBackgroundColor(getResources().getColor(R.color.colorAccentHighlighted));
                int wearing = 0;

                mWearingMask.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                int issued = 1;

                if (mUserID != null && ((mTemperature != null) | (mDir.equals("OUT")))){
                    Intent intentQuestions;
                    SharedPreferences userPrefs = this.getSharedPreferences("tjoptjopprefs",MODE_PRIVATE);
                    Boolean wellness_check = userPrefs.getBoolean("wellness_check",false);
                    if (wellness_check){
                        intentQuestions = new Intent(this, WellnessCheckActivity.class);
                    }else {
                        intentQuestions = new Intent(this, QuestionsActivity.class);
                    }
                    intentQuestions.putExtra(INTENT_EXTRA_ID_NUMBER, mUserID);
                    intentQuestions.putExtra(INTENT_EXTRA_IDTYPE, mIDtype);
                    intentQuestions.putExtra(INTENT_EXTRA_TEMPERATURE, mTemperature);
                    intentQuestions.putExtra(INTENT_EXTRA_TEMPTYPE, mTempType);
                    intentQuestions.putExtra(INTENT_EXTRA_MASK, "I");
                    intentQuestions.putExtra(INTENT_EXTRA_DIRECTION,mDir);
                    if (mName != null) {
                        intentQuestions.putExtra(INTENT_EXTRA_NAME, mName);
                        intentQuestions.putExtra(INTENT_EXTRA_EMERGENCY_CONTACT, mPhone);
                        intentQuestions.putExtra(INTENT_EXTRA_ADDRESS, mAddress);
                        intentQuestions.putExtra(INTENT_EXTRA_CITY, mCity);
                    }
                    startActivity(intentQuestions);
                }
            });
        }
    }
}
