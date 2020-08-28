package com.quintonvr.tjoptjop.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.quintonvr.tjoptjop.R;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_DIRECTION;

public class DirectionActivity extends AppCompatActivity {

    private LinearLayout inboundDir,outboundDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction);

        inboundDir = findViewById(R.id.section1);
        outboundDir = findViewById(R.id.section2);

        inboundDir.setOnClickListener(v -> {
            Intent intent = new Intent(this, IDtypeActivity.class);
            intent.putExtra(INTENT_EXTRA_DIRECTION,"IN");
            startActivity(intent);
        });

        outboundDir.setOnClickListener(v -> {
            Intent intent = new Intent(this, IDtypeActivity.class);
            intent.putExtra(INTENT_EXTRA_DIRECTION,"OUT");
            startActivity(intent);
        });
    }
}
