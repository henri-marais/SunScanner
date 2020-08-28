package com.quintonvr.tjoptjop.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import com.quintonvr.tjoptjop.BuildConfig;
import com.quintonvr.tjoptjop.R;

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
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_WELLNESS;

public class WellnessCheckActivity extends AppCompatActivity {
    private String mUserID, mTemperature;
    private String mIDtype,mTempType;
    private String mDir;
    private String mName;
    private String mPhone;
    private String mAddress;
    private String mCity;
    private String mask;

    private LinearLayout well_healthy,well_ill;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wellness_check);

        well_healthy = findViewById(R.id.section1);
        well_ill = findViewById(R.id.section2);

        Intent intent = getIntent();
        if (intent != null) {
            mDir = intent.getStringExtra(INTENT_EXTRA_DIRECTION);
            mUserID = intent.getStringExtra(INTENT_EXTRA_ID_NUMBER);
            mTemperature = intent.getStringExtra(INTENT_EXTRA_TEMPERATURE);
            mTempType = intent.getStringExtra(INTENT_EXTRA_TEMPTYPE);
            mIDtype = intent.getStringExtra(INTENT_EXTRA_IDTYPE);
            mask = intent.getStringExtra(INTENT_EXTRA_MASK);
            if (intent.getStringExtra(INTENT_EXTRA_NAME) != null) {
                mName = intent.getStringExtra(INTENT_EXTRA_NAME);
                mPhone = intent.getStringExtra(INTENT_EXTRA_EMERGENCY_CONTACT);
                mAddress = intent.getStringExtra(INTENT_EXTRA_ADDRESS);
                mCity = intent.getStringExtra(INTENT_EXTRA_CITY);
            }
        }

        well_healthy.setOnClickListener(v -> {
            if (mUserID != null && ((mTemperature != null) | (mDir.equals("OUT")))){
                Intent QuestionsIntent = new Intent(this, QuestionsActivity.class);
                QuestionsIntent.putExtra(INTENT_EXTRA_ID_NUMBER, mUserID);
                QuestionsIntent.putExtra(INTENT_EXTRA_IDTYPE, mIDtype);
                QuestionsIntent.putExtra(INTENT_EXTRA_TEMPERATURE, mTemperature);
                QuestionsIntent.putExtra(INTENT_EXTRA_TEMPTYPE, mTempType);
                QuestionsIntent.putExtra(INTENT_EXTRA_MASK, "Y");
                QuestionsIntent.putExtra(INTENT_EXTRA_DIRECTION, mDir);
                if (mName != null) {
                    QuestionsIntent.putExtra(INTENT_EXTRA_NAME, mName);
                    QuestionsIntent.putExtra(INTENT_EXTRA_EMERGENCY_CONTACT, mPhone);
                    QuestionsIntent.putExtra(INTENT_EXTRA_ADDRESS, mAddress);
                    QuestionsIntent.putExtra(INTENT_EXTRA_CITY, mCity);
                }
                QuestionsIntent.putExtra(INTENT_EXTRA_WELLNESS, "Y");
                startActivity(QuestionsIntent);
            }
        });

        well_ill.setOnClickListener(v -> {
            if (mUserID != null && ((mTemperature != null) | (mDir.equals("OUT")))){
                Intent QuestionsIntent = new Intent(this, QuestionsActivity.class);
                QuestionsIntent.putExtra(INTENT_EXTRA_ID_NUMBER, mUserID);
                QuestionsIntent.putExtra(INTENT_EXTRA_IDTYPE, mIDtype);
                QuestionsIntent.putExtra(INTENT_EXTRA_TEMPERATURE, mTemperature);
                QuestionsIntent.putExtra(INTENT_EXTRA_TEMPTYPE, mTempType);
                QuestionsIntent.putExtra(INTENT_EXTRA_MASK, "Y");
                QuestionsIntent.putExtra(INTENT_EXTRA_DIRECTION, mDir);
                if (mName != null) {
                    QuestionsIntent.putExtra(INTENT_EXTRA_NAME, mName);
                    QuestionsIntent.putExtra(INTENT_EXTRA_EMERGENCY_CONTACT, mPhone);
                    QuestionsIntent.putExtra(INTENT_EXTRA_ADDRESS, mAddress);
                    QuestionsIntent.putExtra(INTENT_EXTRA_CITY, mCity);
                }
                QuestionsIntent.putExtra(INTENT_EXTRA_WELLNESS, "N");
                startActivity(QuestionsIntent);
            }
        });
    }
}
