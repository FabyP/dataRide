package com.example.dataride;

import android.content.SharedPreferences;
import android.os.Bundle;

import android.preference.Preference;
import android.preference.PreferenceFragment;

import java.util.prefs.PreferenceChangeListener;

public class SettingsFragment extends PreferenceFragment {

    public static final String PREF_GAS_VAR = "pref_gas";
    public static final String PREF_AVERAGE_FUEL_CONSUMPTION_VAR = "pref_average_fuel_consumption";
    public static final String PREF_SPEED__LIMIT = "pref_speed_limit";
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals(PREF_GAS_VAR)){
                    Preference gasPref = findPreference(key);
                    gasPref.setSummary(sharedPreferences.getString(key, ""));
                }
                if(key.equals(PREF_AVERAGE_FUEL_CONSUMPTION_VAR)){
                    Preference averageFuelConsumptionPref = findPreference(key);
                    averageFuelConsumptionPref.setSummary(sharedPreferences.getString(key, "")+" l/100km");
                }
                if(key.equals(PREF_SPEED__LIMIT)){
                    Preference speedLimit = findPreference(key);
                    speedLimit.setSummary(sharedPreferences.getString(key, "")+" km/h");
                }
            }
        };

    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(preferenceChangeListener);



        Preference gasPref = findPreference(PREF_GAS_VAR);
        gasPref.setSummary(getPreferenceScreen().getSharedPreferences().getString(PREF_GAS_VAR, ""));

        Preference averageFuelConsumptionPref = findPreference(PREF_AVERAGE_FUEL_CONSUMPTION_VAR);
        averageFuelConsumptionPref.setSummary(getPreferenceScreen().getSharedPreferences().getString(PREF_AVERAGE_FUEL_CONSUMPTION_VAR, "")+" l/100km");

        Preference speedLimitPref = findPreference(PREF_SPEED__LIMIT);
        speedLimitPref.setSummary(getPreferenceScreen().getSharedPreferences().getString(PREF_SPEED__LIMIT, "")+" km/h");
    }

    @Override
    public void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }
}
