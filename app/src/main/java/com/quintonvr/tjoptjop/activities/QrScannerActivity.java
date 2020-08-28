package com.quintonvr.tjoptjop.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.quintonvr.tjoptjop.R;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_ADDRESS;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_CITY;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_DIRECTION;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_EMERGENCY_CONTACT;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_GRADE;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_IDTYPE;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_ID_NUMBER;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_NAME;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_GENDER;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_AGE;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_DIRECTION;

public class QrScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler  {
    private ZXingScannerView mScannerView;
    private String TAG = "QrScannerActivity";
    Boolean rsa_id_only = false;
    private String mDir = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences userPrefs = this.getSharedPreferences("tjoptjopprefs",MODE_PRIVATE);
        Boolean qrcode = userPrefs.getBoolean("id_qrcode",false);
        Boolean barcode = userPrefs.getBoolean("id_barcode",false);
        rsa_id_only = userPrefs.getBoolean("id_rsa_valid",false);

        Intent intent = getIntent();
        if (intent != null) {
            mDir = intent.getStringExtra(INTENT_EXTRA_DIRECTION);
        }

        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        List<BarcodeFormat> formats = new ArrayList<>();
        formats.add(BarcodeFormat.QR_CODE);
        formats.add(BarcodeFormat.CODE_39);
//        if (qrcode) {};
//        if (barcode) {formats.add(BarcodeFormat.CODE_39);};
//        if (formats.size() < 1) {
//            new AlertDialog.Builder(this)
//                    .setTitle("Oops!")
//                    .setMessage("You have to select some identification method")
//                    .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            Intent intent = new Intent(((Dialog) dialog).getContext(), TempCaptureSettingsActivity.class);
//                            startActivity(intent);
//                        }
//                    })
//                    .setCancelable(false)
//                    .show();
//        }
        mScannerView.setFormats(formats);
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.v(TAG, rawResult.getText()); // Prints scan results
        Log.v(TAG, rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        if (rawResult.getBarcodeFormat() == BarcodeFormat.QR_CODE) {
            try {
                String magicNumber = rawResult.getText().split(";")[0];
                if ((magicNumber.length()>0) && (magicNumber.equals("TjoppojT"))){
                    //QRCode is a TjopTjop Pass
                    String id = rawResult.getText().split(";")[1];
                    String name = rawResult.getText().split(";")[2];
                    String phone = rawResult.getText().split(";")[3];
                    String address = rawResult.getText().split(";")[4];
                    String city = rawResult.getText().split(";")[5];

                    id = id.trim();
                    name = name.trim();
                    phone = phone.trim();
                    address = address.trim();
                    city = city.trim();

                    Log.v(TAG, id);
                    Log.v(TAG, name);
                    Log.v(TAG, phone);
                    Log.v(TAG, address);
                    Log.v(TAG, city);

                    if ((id.length() > 0) && (name.length() > 0) && (phone.length() > 0) && (address.length() > 0) && (city.length() > 0) ) { //valid qr
                        Intent intent = new Intent(this, IdentificationActivity.class);
                        intent.putExtra(INTENT_EXTRA_ID_NUMBER, id);
                        intent.putExtra(INTENT_EXTRA_NAME, name);
                        intent.putExtra(INTENT_EXTRA_EMERGENCY_CONTACT, phone);
                        intent.putExtra(INTENT_EXTRA_IDTYPE, "TTPASS");
                        intent.putExtra(INTENT_EXTRA_ADDRESS,address);
                        intent.putExtra(INTENT_EXTRA_CITY,city);
                        intent.putExtra(INTENT_EXTRA_DIRECTION,mDir);
                        startActivity(intent);
                    } else {
                        new AlertDialog.Builder(this)
                                .setTitle("QR invalid")
                                .setMessage("Please try again.")
                                .setPositiveButton(R.string.button_ok, ((dialogInterface, i) -> {
                                    dialogInterface.dismiss();
                                    mScannerView.resumeCameraPreview(this);
                                }))
                                .setCancelable(false)
                                .show();
                    }
                }else {
                    String id = rawResult.getText().split(";")[0];
                    String name = rawResult.getText().split(";")[1];
                    String grade = rawResult.getText().split(";")[2];
                    String contactNumber = rawResult.getText().split(";")[3];
                    Log.v(TAG, id.trim());
                    Log.v(TAG, name.trim());
                    Log.v(TAG, grade.trim());
                    Log.v(TAG, contactNumber.trim());

                    if ((id.length() > 0) && (name.length() > 0) && (grade.length() > 0) && (contactNumber.length() > 0)) { //valid qr
                        Intent intent = new Intent(this, IdentificationActivity.class);
                        intent.putExtra(INTENT_EXTRA_ID_NUMBER, id.trim());
                        intent.putExtra(INTENT_EXTRA_NAME, name.trim());
                        intent.putExtra(INTENT_EXTRA_GRADE, grade.trim());
                        intent.putExtra(INTENT_EXTRA_EMERGENCY_CONTACT, contactNumber.trim());
                        intent.putExtra(INTENT_EXTRA_IDTYPE, "QRCODE");
                        intent.putExtra(INTENT_EXTRA_DIRECTION,mDir);
                        startActivity(intent);
                    } else {
                        new AlertDialog.Builder(this)
                                .setTitle("QR invalid")
                                .setMessage("Please try again.")
                                .setPositiveButton(R.string.button_ok, ((dialogInterface, i) -> {
                                    dialogInterface.dismiss();
                                    mScannerView.resumeCameraPreview(this);
                                }))
                                .setCancelable(false)
                                .show();
                    }
                }
            } catch (Exception e) {
                new AlertDialog.Builder(this)
                        .setTitle("QR invalid")
                        .setMessage("Please try again.")
                        .setPositiveButton(R.string.button_ok, ((dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            mScannerView.resumeCameraPreview(this);
                        }))
                        .setCancelable(false)
                        .show();
            }
        }else{
            try{
                //Looks like we have a barcode here.
                if (rsa_id_only) {
                    if (checkLuhn(rawResult.getText())) {
                        //Based on the settings either check RSA ID validity or just persist the data in any case.
                        Intent intent = new Intent(this, IdentificationActivity.class);
                        intent.putExtra(INTENT_EXTRA_ID_NUMBER, rawResult.getText());
                        if (Double.parseDouble(rawResult.getText().substring(6,10)) < 5000){
                            intent.putExtra(INTENT_EXTRA_GENDER,"Female");
                        }else {
                            intent.putExtra(INTENT_EXTRA_GENDER,"Male");
                        }
                        int age;
                        if (Double.parseDouble(rawResult.getText().substring(0,2))>20){
                            //Probably person is born in 20th century
                            age = 2020-(1900+(int)(Double.parseDouble(rawResult.getText().substring(0,2))));
                        }else{
                            //Very young person
                            age = (int)(Double.parseDouble(rawResult.getText().substring(0,2)));
                        }
                        intent.putExtra(INTENT_EXTRA_AGE,String.valueOf(age)+" year old");
                        intent.putExtra(INTENT_EXTRA_IDTYPE,"BARCODE");
                        intent.putExtra(INTENT_EXTRA_DIRECTION,mDir);
                        startActivity(intent);
                    } else {
                        new AlertDialog.Builder(this)
                                .setTitle("Barcode is not RSA ID number")
                                .setMessage("Please try again.")
                                .setPositiveButton(R.string.button_ok, ((dialogInterface, i) -> {
                                    dialogInterface.dismiss();
                                    mScannerView.resumeCameraPreview(this);
                                }))
                                .setCancelable(false)
                                .show();
                    }
                }else {
                    Intent intent = new Intent(this, IdentificationActivity.class);
                    intent.putExtra(INTENT_EXTRA_ID_NUMBER, rawResult.getText());
                    intent.putExtra(INTENT_EXTRA_IDTYPE,"BARCODE");
                    intent.putExtra(INTENT_EXTRA_GENDER,"");
                    intent.putExtra(INTENT_EXTRA_AGE,"");
                    intent.putExtra(INTENT_EXTRA_DIRECTION,mDir);
                    startActivity(intent);
                }
            }catch(Exception e){
                new AlertDialog.Builder(this)
                        .setTitle("Cannot process barcode!")
                        .setMessage("Please try again.")
                        .setPositiveButton(R.string.button_ok, ((dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            mScannerView.resumeCameraPreview(this);
                        }))
                        .setCancelable(false)
                        .show();
            }
        }

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
