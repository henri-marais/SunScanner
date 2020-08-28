package com.quintonvr.tjoptjop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.quintonvr.tjoptjop.R;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_DIRECTION;

public class IDtypeActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST = 100;
    private String TAG = "IDType";

    private LinearLayout autoID, manualID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idtype);

        autoID = findViewById(R.id.section1);
        manualID = findViewById(R.id.section2);
        String mDir = "";
        Intent intent = getIntent();
        if (intent != null){
            mDir = intent.getStringExtra(INTENT_EXTRA_DIRECTION);
        }
        String finalDir = mDir;
        autoID.setOnClickListener(v -> {
            Intent scanIntent = new Intent(this, QrScannerActivity.class);
            scanIntent.putExtra(INTENT_EXTRA_DIRECTION,finalDir);
            startActivity(scanIntent);
        });

        manualID.setOnClickListener(v -> {
            Intent makeIntent = new Intent(this, PassGenerationActivity.class);
            makeIntent.putExtra(INTENT_EXTRA_DIRECTION,finalDir);
            startActivity(makeIntent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
