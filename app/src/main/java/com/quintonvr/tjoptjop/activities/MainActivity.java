package com.quintonvr.tjoptjop.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.quintonvr.tjoptjop.BuildConfig;
import com.quintonvr.tjoptjop.R;
import com.quintonvr.tjoptjop.helpers.DatabaseHelper;
import com.quintonvr.tjoptjop.network.CheckNetwork;
import com.quintonvr.tjoptjop.network.QuestionUpdateTask;
import com.quintonvr.tjoptjop.network.SignOutTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static com.quintonvr.tjoptjop.helpers.Utils.PREFS_ACTIVATION_TOKEN;
import static com.quintonvr.tjoptjop.helpers.Utils.PREFS_CUSTOMER_CODE;
import static com.quintonvr.tjoptjop.helpers.Utils.PREFS_QUESTIONS_ID;
import static com.quintonvr.tjoptjop.helpers.Utils.VITAL_PREFS;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST = 100;
    private String TAG = "MainActivity";

    private LinearLayout mIDSection, mBagIDSection, mMaskSection;
    private ImageButton mSyncBtn, mBtnSettings;
    private Button logOutBtn;

    /*FOR Debugging Only*/
    private DatabaseHelper mydb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSyncBtn = findViewById(R.id.syncButton);
        mBtnSettings = findViewById(R.id.BtnSettings);
        logOutBtn = findViewById(R.id.logOutbtn);
        mIDSection = findViewById(R.id.section1);
        mBagIDSection = findViewById(R.id.section2);
        mMaskSection = findViewById(R.id.section3);

        requestPermissions();

        SharedPreferences prefs = getSharedPreferences(VITAL_PREFS, Context.MODE_PRIVATE);
        String institution = prefs.getString(PREFS_CUSTOMER_CODE, "");
        logOutBtn.setText("SIGN OUT\n"+institution);

        mSyncBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Database Sync")
                    .setMessage("Do you wish to perform a database sync?")
                    .setPositiveButton(R.string.button_yes, ((dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        SharedPreferences userPrefs = this.getSharedPreferences("tjoptjopprefs",MODE_PRIVATE);
                        Boolean sync_mode_DR = userPrefs.getBoolean("sync_mode_DR",false);
                        if(!sync_mode_DR) {
                            if (CheckNetwork.isInternetAvailable(this)) {
                                Intent intent = new Intent(this, SyncActivity.class);
                                startActivity(intent);
                            } else {
                                new AlertDialog.Builder(this)
                                        .setTitle("Oops!")
                                        .setMessage("No internet connection.")
                                        .setPositiveButton(R.string.button_ok, ((dialogInterface1, j) -> {
                                            dialogInterface1.dismiss();
                                        }))
                                        .setCancelable(false)
                                        .show();
                            }
                        }else{
                            new AlertDialog.Builder(this)
                                    .setTitle("Disaster Recovery")
                                    .setMessage("Do you wish to perform disaster recovery? If not turn of the relevant setting!")
                                    .setPositiveButton(R.string.button_yes, ((disasterInterface, j) -> {
                                        disasterInterface.dismiss();
                                        try {
                                            mydb = new DatabaseHelper(this);
                                            mydb.dumpToFile();
                                            mydb.close();
                                            Toast.makeText(getApplicationContext(), "Operation complete!", Toast.LENGTH_LONG).show();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }))
                                    .setNegativeButton(R.string.button_cancel, ((disasterInterface, j) -> {
                                        disasterInterface.dismiss();
                                    }))
                                    .show();
                        }
                    }))
                    .setNegativeButton(R.string.button_cancel, ((dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    }))
                    .show();
        });

        logOutBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Log out")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton(R.string.button_yes, ((dialogInterface, i) -> {
                        dialogInterface.dismiss();

                        if (CheckNetwork.isInternetAvailable(this)){
                            new SignOutTask(this).execute();
                        } else {
                            new AlertDialog.Builder(this)
                                    .setTitle("Oops!")
                                    .setMessage("No internet connection.")
                                    .setPositiveButton(R.string.button_ok, ((dialogInterface1, j) -> {
                                        dialogInterface1.dismiss();
                                    }))
                                    .setCancelable(false)
                                    .show();
                        }
                    }))
                    .setNegativeButton(R.string.button_cancel, ((dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    }))
                    .show();
        });

        mBtnSettings.setOnClickListener(v->{
            Intent intent = new Intent(this, TempCaptureSettingsActivity.class);
            startActivity(intent);
        });

        mIDSection.setOnClickListener(v -> {
            Intent intent = new Intent(this, QrScannerActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(VITAL_PREFS, Context.MODE_PRIVATE);
        int questionID = prefs.getInt(PREFS_QUESTIONS_ID,-1);
        if (CheckNetwork.isInternetAvailable(this)) {
            new QuestionUpdateTask(this).execute();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void requestPermissions() {
        ArrayList<String> permissionsList = new ArrayList<String>();
        int externalStorage = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int useCamera = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);

        if (externalStorage != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (useCamera != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(Manifest.permission.CAMERA);
        }

        if (permissionsList.size() > 0) {
            String[] permissions = new String[permissionsList.size()];
            permissions = permissionsList.toArray(permissions);
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "All permissions required to use this app", Toast.LENGTH_LONG).show();
                finishAffinity();
            }
        }
    }

    private void fakeGen(int recordNum){
        SharedPreferences prefs = getSharedPreferences(VITAL_PREFS, Context.MODE_PRIVATE);
        String customerCode = prefs.getString(PREFS_CUSTOMER_CODE, "");
        String activationToken = prefs.getString(PREFS_ACTIVATION_TOKEN, "");
        Random rand = new Random();
        mydb = new DatabaseHelper(this);
        String RecordMeta = "{COM;"+ BuildConfig.VERSION_CODE+";"+"A"+"}";
        for (int i=0;i<recordNum;i++){
            String userID = Integer.toString(rand.nextInt(10));
            String temperature;
            String q12 = "Random Person #"+Integer.toString(rand.nextInt(recordNum));
            String q13 = Integer.toString(rand.nextInt(9999999));
            String q14 = "House number "+Integer.toString(rand.nextInt(recordNum*2));
            mydb.insertNewValue(customerCode, activationToken, userID, "34.6C", "A", null, "Y",
                    "N", "N", "N", "N", "N", "N", "N","","","",q12,q13,q14,"Fakeville","IN",RecordMeta);
        }
        mydb.close();
    }

    public void copyAppDbToDownloadFolder() throws IOException {
        try {
            File backupDB = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "toDatabaseName"); // for example "my_data_backup.db"
            File currentDB = getApplicationContext().getDatabasePath("/user-records.db"); //databaseName=your current application database name, for example "my_data.db"
            if (currentDB.exists()) {
                FileInputStream fis = new FileInputStream(currentDB);
                FileOutputStream fos = new FileOutputStream(backupDB);
                fos.getChannel().transferFrom(fis.getChannel(), 0, fis.getChannel().size());
                // or fis.getChannel().transferTo(0, fis.getChannel().size(), fos.getChannel());
                fis.close();
                fos.close();
                Log.i("Database successfully", " copied to download folder");
            } else Log.i("Copying Database", " fail, database not found");
        } catch (IOException e) {
            Log.d("Copying Database", "fail, reason:", e);
        }
    }
}
