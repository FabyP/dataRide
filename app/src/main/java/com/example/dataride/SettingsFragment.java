package com.example.dataride;

import android.content.SharedPreferences;
import android.os.Bundle;

import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import android.preference.EditTextPreference;

import java.util.prefs.PreferenceChangeListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsFragment extends PreferenceFragment {

    public static final String PREF_GAS_VAR = "pref_gas";
    public static final String PREF_AVERAGE_FUEL_CONSUMPTION_VAR = "pref_average_fuel_consumption";
    public static final String PREF_SPEED__LIMIT = "pref_speed_limit";
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    private EditTextPreference fuel;
    private String oldFuel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(PREF_GAS_VAR)) {
                    Preference gasPref = findPreference(key);
                    gasPref.setSummary(sharedPreferences.getString(key, ""));
                }
                if (key.equals(PREF_AVERAGE_FUEL_CONSUMPTION_VAR)) {

                    String tank = sharedPreferences.getString(key, "");
                    tank = tank.replace(",", ".");
                    Pattern p = Pattern.compile("\\d{0,2}+(\\.\\d{1,2})?");
                    Matcher m = p.matcher(tank);
                    boolean b = Pattern.matches("\\d{0,2}+(\\.\\d{1,2})?", tank);
                    if (b) {
                        Preference averageFuelConsumptionPref = findPreference(key);
                        averageFuelConsumptionPref.setSummary(sharedPreferences.getString(key, "") + " l/100km");
                        oldFuel = sharedPreferences.getString(key, "");
                    } else {
                        fuel.setText(oldFuel);
                        Toast.makeText(getActivity(), "Bitte nur Zahlen eingeben", Toast.LENGTH_LONG).show();
                        //setPreferenceScreen(null);
                        //addPreferencesFromResource(R.xml.preferences);
                    }

                    //Preference averageFuelConsumptionPref = findPreference(key);
                    //averageFuelConsumptionPref.setSummary(sharedPreferences.getString(key, "") + " l/100km");
                }
                if (key.equals(PREF_SPEED__LIMIT)) {
                    Preference speedLimit = findPreference(key);
                    speedLimit.setSummary(sharedPreferences.getString(key, "") + " km/h");
                }
            }
        };

        fuel = (EditTextPreference) findPreference(PREF_AVERAGE_FUEL_CONSUMPTION_VAR);
        oldFuel = fuel.getText();
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(preferenceChangeListener);


        Preference gasPref = findPreference(PREF_GAS_VAR);
        gasPref.setSummary(getPreferenceScreen().getSharedPreferences().getString(PREF_GAS_VAR, ""));

        Preference averageFuelConsumptionPref = findPreference(PREF_AVERAGE_FUEL_CONSUMPTION_VAR);
        averageFuelConsumptionPref.setSummary(getPreferenceScreen().getSharedPreferences().getString(PREF_AVERAGE_FUEL_CONSUMPTION_VAR, "") + " l/100km");

        Preference speedLimitPref = findPreference(PREF_SPEED__LIMIT);
        speedLimitPref.setSummary(getPreferenceScreen().getSharedPreferences().getString(PREF_SPEED__LIMIT, "") + " km/h");
    }

    @Override
    public void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }
}
