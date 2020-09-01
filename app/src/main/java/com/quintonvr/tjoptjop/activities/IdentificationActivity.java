package com.quintonvr.tjoptjop.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.ImageView;
import android.widget.TextView;

import com.quintonvr.tjoptjop.R;

import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_ADDRESS;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_AGE;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_CITY;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_EMERGENCY_CONTACT;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_GENDER;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_GRADE;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_ID_NUMBER;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_NAME;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_IDTYPE;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_DIRECTION;


public class IdentificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification);
        ImageView typeSample = findViewById(R.id.imgTypeID);
        TextView mUserDetails = findViewById(R.id.userDetails_textView);
        ImageView mCancel = findViewById(R.id.imageView_cancel);
        ImageView mConfirm = findViewById(R.id.imageView_confirm);

        String userID = null;
        String name = null;
        String address = null;
        String city = null;
        String phone = null;
        String typeID = null;
        String mDir = null;
        Intent intent = getIntent();
        if (intent != null) {
            mDir = intent.getStringExtra(INTENT_EXTRA_DIRECTION);
            userID = intent.getStringExtra(INTENT_EXTRA_ID_NUMBER);
            name = intent.getStringExtra(INTENT_EXTRA_NAME);
            String grade = intent.getStringExtra(INTENT_EXTRA_GRADE);
            String emergency = intent.getStringExtra(INTENT_EXTRA_EMERGENCY_CONTACT);
            typeID = intent.getStringExtra(INTENT_EXTRA_IDTYPE);
            if (intent.getStringExtra(INTENT_EXTRA_IDTYPE).equals("QRCODE")){
                Resources res = getResources(); /** from an Activity */
                typeSample.setImageDrawable(res.getDrawable(R.mipmap.qr_sample));
                mUserDetails.setText(getString(R.string.user_details, userID, name, grade, emergency));
            }else if(intent.getStringExtra(INTENT_EXTRA_IDTYPE).equals("BARCODE")){
                //Barcode lands you here
                userID = intent.getStringExtra(INTENT_EXTRA_ID_NUMBER);
                String age = intent.getStringExtra(INTENT_EXTRA_AGE);
                String gender = intent.getStringExtra(INTENT_EXTRA_GENDER);
                Resources res = getResources(); /** from an Activity */
                typeSample.setImageDrawable(res.getDrawable(R.mipmap.bc_sample));
                mUserDetails.setText(getString(R.string.barcode_details,userID,age,gender));
            }else{
                userID = intent.getStringExtra(INTENT_EXTRA_ID_NUMBER);
                name = intent.getStringExtra(INTENT_EXTRA_NAME);
                phone = intent.getStringExtra(INTENT_EXTRA_EMERGENCY_CONTACT);
                address = intent.getStringExtra(INTENT_EXTRA_ADDRESS);
                city = intent.getStringExtra(INTENT_EXTRA_CITY);
                Resources res = getResources(); /** from an Activity */
                typeSample.setImageDrawable(res.getDrawable(R.mipmap.qr_sample));
                mUserDetails.setText(getString(R.string.ttpass_details,userID,name));
            }
        }

        mCancel.setOnClickListener(v -> {
            finish();
        });

        String finalUserID = userID;
        String finalIDtype = typeID;
        String finalName = name;
        String finalAddress = address;
        String finalPhone = phone;
        String finalCity = city;
        String finalDir = mDir;
        mConfirm.setOnClickListener(v -> {
//            if (finalDir.equals("IN")) {
//                //Inbound requires a temperature scan
//                Intent TempScan;
//                SharedPreferences userPrefs = this.getSharedPreferences("tjoptjopprefs",MODE_PRIVATE);
//                Boolean automatic_temp_on = userPrefs.getBoolean("auto_temp",false);
//                if (automatic_temp_on){
//                    TempScan = new Intent(this, TempScanningActivity.class);
//                }else {
//                    TempScan = new Intent(this, ManualEntryActivity.class);
//                }
//                TempScan.putExtra(INTENT_EXTRA_ID_NUMBER, finalUserID);
//                TempScan.putExtra(INTENT_EXTRA_IDTYPE, finalIDtype);
//                TempScan.putExtra(INTENT_EXTRA_DIRECTION,finalDir);
//                if (finalAddress != null) {
//                    TempScan.putExtra(INTENT_EXTRA_NAME, finalName);
//                    TempScan.putExtra(INTENT_EXTRA_EMERGENCY_CONTACT, finalPhone);
//                    TempScan.putExtra(INTENT_EXTRA_ADDRESS, finalAddress);
//                    TempScan.putExtra(INTENT_EXTRA_CITY, finalCity);
//                }
//                startActivity(TempScan);
//            }else{
//                //Outbound leads directly to questions
                Intent intentQuestions;
//                SharedPreferences userPrefs = this.getSharedPreferences("tjoptjopprefs",MODE_PRIVATE);
//                Boolean wellness_check = userPrefs.getBoolean("wellness_check",false);
//                if (wellness_check){
//                    intentQuestions = new Intent(this, WellnessCheckActivity.class);
//                }else {
                    intentQuestions = new Intent(this, QuestionsActivity.class);
//                }
                intentQuestions.putExtra(INTENT_EXTRA_ID_NUMBER, finalUserID);
                intentQuestions.putExtra(INTENT_EXTRA_IDTYPE, finalIDtype);
//                intentQuestions.putExtra(INTENT_EXTRA_DIRECTION,finalDir);
//                if (finalAddress != null) {
//                    intentQuestions.putExtra(INTENT_EXTRA_NAME, finalName);
//                    intentQuestions.putExtra(INTENT_EXTRA_EMERGENCY_CONTACT, finalPhone);
//                    intentQuestions.putExtra(INTENT_EXTRA_ADDRESS, finalAddress);
//                    intentQuestions.putExtra(INTENT_EXTRA_CITY, finalCity);
//                }
                startActivity(intentQuestions);
//            }
        });
    }
}
