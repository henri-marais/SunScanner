package com.quintonvr.tjoptjop.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.quintonvr.tjoptjop.R;
import android.os.Bundle;

import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_ADDRESS;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_CITY;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_DIRECTION;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_EMERGENCY_CONTACT;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_GRADE;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_IDTYPE;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_ID_NUMBER;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_NAME;

public class PassGenerationActivity extends AppCompatActivity {

    private EditText input_IDnumber;
    private EditText input_NameSurname;
    private EditText input_Phone;
    private EditText input_Address;
    private EditText input_City;
    private ImageButton makePass;
    private String mDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_generation);

        input_IDnumber = findViewById(R.id.input_IDnumber);
        input_IDnumber.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.validation_failed));
        input_NameSurname = findViewById(R.id.input_NameSurname);
        input_NameSurname.setVisibility(View.INVISIBLE);
        input_NameSurname.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.validation_failed));
        input_Phone = findViewById(R.id.input_Cellno);
        input_Phone.setVisibility(View.INVISIBLE);
        input_Phone.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.validation_failed));
        input_Address = findViewById(R.id.input_StreetAddress);
        input_Address.setVisibility(View.INVISIBLE);
        input_Address.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.validation_failed));
        input_City = findViewById(R.id.input_City);
        input_City.setVisibility(View.INVISIBLE);
        input_City.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.validation_failed));
        makePass = findViewById(R.id.continueButton);
        makePass.setVisibility(View.INVISIBLE);
        makePass.setEnabled(false);

        Intent intent = getIntent();
        if (intent != null) {
            mDir = intent.getStringExtra(INTENT_EXTRA_DIRECTION);
        }

        Intent TempScan;
        SharedPreferences userPrefs = this.getSharedPreferences("tjoptjopprefs",MODE_PRIVATE);
        Boolean automatic_temp_on = userPrefs.getBoolean("auto_temp",false);
        Boolean wellness_check = userPrefs.getBoolean("wellness_check",false);



        makePass.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent;
                if (mDir.equals("IN")){
                    //Add pref for autotemp here
                    if (automatic_temp_on){
                        intent = new Intent(getApplicationContext(),TempScanningActivity.class);
                    }else {
                        intent = new Intent(getApplicationContext(),ManualEntryActivity.class);
                    }
                }else{
                    if (wellness_check){
                        intent = new Intent(getApplicationContext(), WellnessCheckActivity.class);
                    }else {
                        intent = new Intent(getApplicationContext(), QuestionsActivity.class);
                    }
                    intent = new Intent(getApplicationContext(),MaskActivity.class);
                }
                intent.putExtra(INTENT_EXTRA_ID_NUMBER, input_IDnumber.getText().toString());
                intent.putExtra(INTENT_EXTRA_NAME, input_NameSurname.getText().toString());
                intent.putExtra(INTENT_EXTRA_EMERGENCY_CONTACT, input_Phone.getText().toString());
                intent.putExtra(INTENT_EXTRA_IDTYPE, "TTPASSman");
                intent.putExtra(INTENT_EXTRA_ADDRESS,input_Address.getText().toString());
                intent.putExtra(INTENT_EXTRA_CITY,input_City.getText().toString());

                intent.putExtra(INTENT_EXTRA_DIRECTION,mDir);
                startActivity(intent);
            }
        });


        input_IDnumber.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,int count, int after) {}

            public void onTextChanged(CharSequence s, int start,int before, int count) {
                if (checkLuhn(s.toString())){
                    input_NameSurname.setVisibility(View.VISIBLE);
                    input_IDnumber.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.white));
                }
            }
        });

        input_NameSurname.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,int count, int after) {}

            public void onTextChanged(CharSequence s, int start,int before, int count) {
                if (s.toString().matches(".*\\s.*")){
                    input_Phone.setVisibility(View.VISIBLE);
                    input_NameSurname.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.white));
                }
            }
        });

        input_Phone.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,int count, int after) {}

            public void onTextChanged(CharSequence s, int start,int before, int count) {
                if (s.toString().length()>8){
                    try{
                        Double.parseDouble(s.toString());
                        input_Address.setVisibility(View.VISIBLE);
                        input_Phone.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.white));
                    }catch(Exception e){

                    };
                }
            }
        });

        input_Address.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,int count, int after) {}

            public void onTextChanged(CharSequence s, int start,int before, int count) {
                if (s.toString().length()>8){
                    input_City.setVisibility(View.VISIBLE);
                    input_Address.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.white));
                }
            }
        });

        input_City.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,int count, int after) {}

            public void onTextChanged(CharSequence s, int start,int before, int count) {
                if (s.toString().length()>3){
                    makePass.setVisibility(View.VISIBLE);
                    makePass.setEnabled(true);
                    input_City.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.white));
                }
            }
        });

    }

    public static boolean checkLuhn(String Identity) {
        try {
            Double.parseDouble(Identity);   //If identity contains only numbers this will pass
            char[] idchars = Identity.toCharArray();
            int sum = 0;
            // loop over each digit right-to-left, including the check-digit
            for (int i = 1; i <= idchars.length; i++) {
                int digit = Character.getNumericValue(idchars[idchars.length - i]);
                if ((i % 2) != 0) {
                    sum += digit;
                } else {
                    sum += digit < 5 ? digit * 2 : digit * 2 - 9;
                }
            }
            return (sum % 10) == 0;
        } catch (Exception e) {
            //If te land here the identidy is not all numbers
        }
        return false;
    }
}

