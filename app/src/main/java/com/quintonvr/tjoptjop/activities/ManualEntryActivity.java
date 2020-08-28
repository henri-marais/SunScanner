package com.quintonvr.tjoptjop.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.quintonvr.tjoptjop.R;

import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_ADDRESS;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_CITY;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_DIRECTION;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_EMERGENCY_CONTACT;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_IDTYPE;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_ID_NUMBER;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_NAME;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_TEMPERATURE;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_TEMPTYPE;

public class ManualEntryActivity extends AppCompatActivity {

    private String enteredTemp = new String("");
    private String mUserID;
    private String mIDtype;
    private String mName;
    private String mPhone;
    private String mAddress;
    private String mCity;
    private String mDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_entry);
        ((ImageButton)findViewById(R.id.imageButtonOK)).setVisibility(View.INVISIBLE);
        Intent intent = getIntent();
        if (intent != null) {
            mDir = intent.getStringExtra(INTENT_EXTRA_DIRECTION);
            mUserID = intent.getStringExtra(INTENT_EXTRA_ID_NUMBER);
            mIDtype = intent.getStringExtra(INTENT_EXTRA_IDTYPE);
            mName= intent.getStringExtra(INTENT_EXTRA_NAME);
            mPhone = intent.getStringExtra(INTENT_EXTRA_EMERGENCY_CONTACT);
            mAddress = intent.getStringExtra(INTENT_EXTRA_ADDRESS);
            mCity = intent.getStringExtra(INTENT_EXTRA_CITY);
            if (mUserID != null) {
                Log.d("ManualEntryActivity", "USER ID: "+ mUserID);
            } else {
                Toast.makeText(this, "Perform IDENTIFICATION first.", Toast.LENGTH_LONG).show();
                finish();
            }
        }

        ((ImageButton)findViewById(R.id.imageButton0)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTempDigit("0");
                ((EditText)findViewById(R.id.capturedTemp)).setText(enteredTemp);
            }
        });
        ((ImageButton)findViewById(R.id.imageButton1)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTempDigit("1");
                ((EditText)findViewById(R.id.capturedTemp)).setText(enteredTemp);
            }
        });
        ((ImageButton)findViewById(R.id.imageButton2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTempDigit("2");
                ((EditText)findViewById(R.id.capturedTemp)).setText(enteredTemp);
            }
        });
        ((ImageButton)findViewById(R.id.imageButton3)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTempDigit("3");
                ((EditText)findViewById(R.id.capturedTemp)).setText(enteredTemp);
            }
        });
        ((ImageButton)findViewById(R.id.imageButton4)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTempDigit("4");
                ((EditText)findViewById(R.id.capturedTemp)).setText(enteredTemp);
            }
        });
        ((ImageButton)findViewById(R.id.imageButton5)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTempDigit("5");
                ((EditText)findViewById(R.id.capturedTemp)).setText(enteredTemp);
            }
        });
        ((ImageButton)findViewById(R.id.imageButton6)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTempDigit("6");
                ((EditText)findViewById(R.id.capturedTemp)).setText(enteredTemp);
            }
        });
        ((ImageButton)findViewById(R.id.imageButton7)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTempDigit("7");
                ((EditText)findViewById(R.id.capturedTemp)).setText(enteredTemp);
            }
        });
        ((ImageButton)findViewById(R.id.imageButton8)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTempDigit("8");
                ((EditText)findViewById(R.id.capturedTemp)).setText(enteredTemp);
            }
        });
        ((ImageButton)findViewById(R.id.imageButton9)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTempDigit("9");
                ((EditText)findViewById(R.id.capturedTemp)).setText(enteredTemp);
            }
        });

        ((ImageButton)findViewById(R.id.imageButtonC)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeTempDigit();
                ((EditText)findViewById(R.id.capturedTemp)).setText(enteredTemp);
            }
        });

        ((ImageButton)findViewById(R.id.settingButton)).setOnClickListener(v ->{
            Intent settingsIntent = new Intent(this,TempCaptureSettingsActivity.class);
            startActivity(settingsIntent);
        });

        ((ImageButton)findViewById(R.id.imageButtonOK)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double measuredTemperature = Double.parseDouble(enteredTemp.substring(0,enteredTemp.length()-1));

                if (measuredTemperature > 37.4) {
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibrate for 500 milliseconds
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        //deprecated in API 26
                        vibrator.vibrate(500);
                    }
                    //sound the alarm
                    final MediaPlayer mp = MediaPlayer.create(ManualEntryActivity.this, R.raw.beep1);
                    mp.start();
                    mp.setOnCompletionListener(MediaPlayer::release);

                    new AlertDialog.Builder(ManualEntryActivity.this)
                            .setTitle("WARNING!")
                            .setMessage("Temperature NOT within normal range.")
                            .setPositiveButton(R.string.button_ok, ((dialogInterface, i) -> {
                                dialogInterface.dismiss();
                                Intent maskIntent = new Intent(ManualEntryActivity.this, MaskActivity.class);
                                maskIntent.putExtra(INTENT_EXTRA_ID_NUMBER, mUserID);
                                maskIntent.putExtra(INTENT_EXTRA_IDTYPE,mIDtype);
                                maskIntent.putExtra(INTENT_EXTRA_TEMPERATURE, enteredTemp);
                                maskIntent.putExtra(INTENT_EXTRA_TEMPTYPE,"M");
                                if (mName != null) {
                                    maskIntent.putExtra(INTENT_EXTRA_NAME, mName);
                                    maskIntent.putExtra(INTENT_EXTRA_EMERGENCY_CONTACT, mPhone);
                                    maskIntent.putExtra(INTENT_EXTRA_ADDRESS, mAddress);
                                    maskIntent.putExtra(INTENT_EXTRA_CITY, mCity);
                                }
                                maskIntent.putExtra(INTENT_EXTRA_DIRECTION,mDir);
                                startActivity(maskIntent);
                            }))
                            .setCancelable(false)
                            .show();
                } else {
                    Intent maskIntent = new Intent(ManualEntryActivity.this, MaskActivity.class);
                    maskIntent.putExtra(INTENT_EXTRA_ID_NUMBER, mUserID);
                    maskIntent.putExtra(INTENT_EXTRA_IDTYPE,mIDtype);
                    maskIntent.putExtra(INTENT_EXTRA_TEMPERATURE, enteredTemp);
                    maskIntent.putExtra(INTENT_EXTRA_TEMPTYPE,"M");
                    if (mName != null) {
                        maskIntent.putExtra(INTENT_EXTRA_NAME, mName);
                        maskIntent.putExtra(INTENT_EXTRA_EMERGENCY_CONTACT, mPhone);
                        maskIntent.putExtra(INTENT_EXTRA_ADDRESS, mAddress);
                        maskIntent.putExtra(INTENT_EXTRA_CITY, mCity);
                    }
                    maskIntent.putExtra(INTENT_EXTRA_DIRECTION,mDir);
                    startActivity(maskIntent);
                }
            }
        });
    }

    private void addTempDigit(String digit){
        if (enteredTemp == null){
            enteredTemp = new String();
        }else if (enteredTemp.length() < 5){
            if (enteredTemp.length() == 2){
                enteredTemp += ".";
            }
            enteredTemp = enteredTemp+digit;
        }
      if (enteredTemp.length() == 4){
            enteredTemp += "C";
            ((ImageButton)findViewById(R.id.imageButtonOK)).setVisibility(View.VISIBLE);
        }
    }

    private void removeTempDigit(){
        if (enteredTemp != null){
            if (enteredTemp.length() > 1) {
                //If correction is made after complete temperature is entered truncate the C
                if (enteredTemp.substring(enteredTemp.length()-1,enteredTemp.length()).equalsIgnoreCase("C")){
                    enteredTemp = enteredTemp.substring(0,enteredTemp.length()-1);
                    ((ImageButton)findViewById(R.id.imageButtonOK)).setVisibility(View.INVISIBLE);
                }
                enteredTemp = enteredTemp.substring(0, enteredTemp.length() - 1);
                //Finally remove the . if that is the last character
                if (enteredTemp.substring(enteredTemp.length()-1,enteredTemp.length()).equalsIgnoreCase(".")){
                    enteredTemp = enteredTemp.substring(0,enteredTemp.length()-1);
                }
                ((EditText)findViewById(R.id.capturedTemp)).setText(enteredTemp);
            }else{
                enteredTemp = "";
                ((EditText)findViewById(R.id.capturedTemp)).setText(enteredTemp);
            }
        }
    }
}
