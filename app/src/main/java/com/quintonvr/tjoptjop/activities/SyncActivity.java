package com.quintonvr.tjoptjop.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.quintonvr.tjoptjop.R;
import com.quintonvr.tjoptjop.helpers.DatabaseHelper;
import com.quintonvr.tjoptjop.network.DatabaseSyncTask;
import com.quintonvr.tjoptjop.network.SyncInterface;

public class SyncActivity extends AppCompatActivity implements SyncInterface {

    private DatabaseHelper mydb;
    private ImageButton mBtnSyncCancel;
    private ImageButton mBtnSyncRetry;
    private ImageButton mBtnSyncDone;
    private TextView mSyncDetails;
    private ProgressBar mSyncProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        mBtnSyncCancel = findViewById(R.id.btnSyncCancel);
        mBtnSyncRetry = findViewById(R.id.btnSyncRetry);
        mBtnSyncDone = findViewById(R.id.btnSyncDone);
        mSyncDetails = findViewById(R.id.syncDetails);
        mSyncProgress = findViewById(R.id.syncProgress);
        mSyncProgress.setProgress(0);
        mSyncDetails.setMovementMethod(new ScrollingMovementMethod());
        mSyncDetails.setText("Starting synchronisation process\r\n");
        mSyncDetails.append("Target: "+getApplicationContext().getString(R.string.domain_name) + "/app/syncv3.php\r\n");
        mydb = new DatabaseHelper(this);
        mSyncDetails.append("Number of unsyncronised records: "+ Long.toString(mydb.getNumRecords())+"\r\n");
        mydb.close();

        mBtnSyncCancel.setOnClickListener(v->{
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        mBtnSyncDone.setOnClickListener(v->{
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        /*Start the actual synchronisation task*/
        DatabaseSyncTask syncTask = new DatabaseSyncTask(this);
        syncTask.setListener(this);
        syncTask.execute();
    }

    @Override
    public void SyncProgressUpdate(int complete, String message) {
        if (complete >= 100){
            mSyncProgress.setProgress(100);
            mBtnSyncCancel.setVisibility(View.INVISIBLE);
            mSyncDetails.append("Synchronisation complete!\n");
            mBtnSyncDone.setVisibility(View.VISIBLE);
        }else if (complete < 0){
            switch(complete){
                case -1:{
                    mBtnSyncCancel.setVisibility(View.INVISIBLE);
                    mBtnSyncDone.setVisibility(View.VISIBLE);
                    new AlertDialog.Builder(this)
                            .setTitle("Device is up to date")
                            .setMessage(message)
                            .setPositiveButton(R.string.button_ok, ((dialogInterface, i) -> {
                                dialogInterface.dismiss();
                            }))
                            .setCancelable(false)
                            .show();
                    break;
                }
                case -2:{
                    new AlertDialog.Builder(this)
                            .setTitle("Synchronisation error")
                            .setMessage(message)
                            .setPositiveButton(R.string.button_ok, ((dialogInterface, i) -> {
                                dialogInterface.dismiss();
                            }))
                            .setCancelable(false)
                            .show();
                    break;
                }
                case -3:{
                    new AlertDialog.Builder(this)
                            .setTitle("Authentication error")
                            .setMessage(message)
                            .setPositiveButton(R.string.button_ok, ((dialogInterface, i) -> {
                                dialogInterface.dismiss();
                            }))
                            .setCancelable(false)
                            .show();
                    break;
                }
            }
        }else{
            mSyncProgress.setProgress(complete);
            mSyncDetails.append(message+"\n");
        }
    }
}