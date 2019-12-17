package com.example.dataride;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.ListPreference;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

public class SettingsActivity extends AppCompatActivity {

    /*SwitchCompat bluetooth;
    boolean stateSwitchBluetooth;
    SharedPreferences preferences;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        /*
        preferences = getSharedPreferences("PRESS", 0);
        stateSwitchBluetooth = preferences.getBoolean("bluetooth", false);

        bluetooth = (SwitchCompat) findViewById(R.id.bluetooth);

        bluetooth.setChecked(stateSwitchBluetooth);

        bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stateSwitchBluetooth = !stateSwitchBluetooth;
                bluetooth.setChecked(stateSwitchBluetooth);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("bluetooth", stateSwitchBluetooth);
                editor.apply();
            }
        });*/

        if(findViewById(R.id.fragment_container) != null){
            if(savedInstanceState != null)
                return;

            getFragmentManager().beginTransaction().add(R.id.fragment_container, new SettingsFragment()).commit();
        }



    }
}
