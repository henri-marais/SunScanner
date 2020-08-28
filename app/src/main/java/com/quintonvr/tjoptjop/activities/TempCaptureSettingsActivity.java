package com.quintonvr.tjoptjop.activities;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.quintonvr.tjoptjop.R;

public class TempCaptureSettingsActivity extends AppCompatActivity {
    public static final String KEY_BOX_HEIGHT = "box_height";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            PreferenceManager pMan = getPreferenceManager();
            pMan.setSharedPreferencesName("tjoptjopprefs");
            pMan.setSharedPreferencesMode(MODE_PRIVATE);
            addPreferencesFromResource(R.xml.tjoptjopprefs);
//            PreferenceScreen screen = getPreferenceScreen();
//            PreferenceCategory TempSettingsCat= (PreferenceCategory)findPreference("TempSettings");
//            SwitchPreference AutoTemp = (SwitchPreference)findPreference("auto_temp");
//            if (AutoTemp.isChecked()) {
//                screen.removePreference(TempSettingsCat);
//            }
        }
    }

//    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
//        if (key.equals("auto_temp")) {
//            this.recreate();
//        }
//    }
}