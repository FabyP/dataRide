package com.example.dataride;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.OnNmeaMessageListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


@RequiresApi(api = Build.VERSION_CODES.N)
public class MainActivity extends AppCompatActivity implements LocationListener, OnNmeaMessageListener {
    // minimale zeit und distanz ab der die gps daten aktualisiert werden
    //angegeben in milli sekunden
    // momentan jede sekunde, ist das ausreichend?
    private static final long MIN_TIME_TO_REFRESH = 1000L;
    //angegeben in meter
    //hier auf 0 gesetzt damit er einfach immer nur das zeitintervall nimmt
    private static final float MIN_DISTANCE_TO_REFRESH = 0F;

    LocationManager lm = null;
    OnNmeaMessageListener nmeaListener;
    LocationListener gpsListener = null;

    //Testzwecken
    TextView textLong;
    TextView textLat;
    TextView textNmea;
    TextView text4;

    FloatingActionButton fab;
    boolean clicked;
    boolean gpsQuality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        textLat = (TextView) findViewById(R.id.textView);
        textLong = (TextView) findViewById(R.id.textView2);
        textNmea = (TextView) findViewById(R.id.textView3);
        text4 = (TextView) findViewById(R.id.textView4);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        clicked = false;
        gpsQuality = false;


        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);


       fab.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                //if(clicked){
                    //Toast.makeText(MainActivity.this,"Stop Tracking",Toast.LENGTH_SHORT).show();
                    //Log.v("CLICKED","");
                   //stopTracking();
                    //clicked = false;
                //} else{
                        startTracking();
                        Toast.makeText(MainActivity.this,"Start Tracking",Toast.LENGTH_SHORT).show();
                        Log.v("CLICKED","");
                 //   clicked = true;
                //}

            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void startTracking(){
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(MainActivity.this, "Bitte die Rechte für das GPS vergeben", Toast.LENGTH_LONG).show();
        }
        LocationManager lm = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
        if(lm != null) {
            boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled) {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 6000, 0, MainActivity.this);
                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                onLocationChanged(location);
                lm.addNmeaListener(MainActivity.this);
            } else {
                textLong.setText("SHIT");
            }
        } else{
            Toast.makeText(MainActivity.this, "Kein Signal gefunden", Toast.LENGTH_LONG).show();
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 6000, 0, MainActivity.this);
        lm.addNmeaListener(MainActivity.this);
    }
    public  void stopTracking(){
        //hier sicherstellen das er alles vorherige rausnimmt
        textLat.setText("Remove");
        textLong.setText("Remove");
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            String latString = Double.toString(lat);
            String lonString = Double.toString(lon);
            textLat.setText(latString);
            textLong.setText(lonString);

        } else{
            textLat.setText("geht nicht");
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //Android Lifecycle Methoden
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //lm.removeUpdates(MainActivity.this);
    }

    //wird immer wieder aufgerufen auch wenn die app geschlossen war aber noch nicht gelöscht wurde,
    // deswegen hier nochmal der check ob gps immer noch vorhanden ist
    @Override
    protected void onResume() {
        super.onResume();
    }

    //hier wird alles entfernt sobald der Nutzer die app schließt
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //lm.removeUpdates(MainActivity.this);
    }

    @Override
    public void onNmeaMessage(String message, long timestamp) {
        processNmea(message);
    }
    public void processNmea(String nmea){
        boolean GSAGood = false;
        boolean GGAGood = false;
        boolean GSVGood = false;

        String[] rawNmeaSplit = nmea.split(",");

        if (rawNmeaSplit[0].equalsIgnoreCase("$GPGGA")){
            String fixQualityString = rawNmeaSplit[6];
            Integer fixQuality = Integer.parseInt(fixQualityString);
            String numberSatellitesString = rawNmeaSplit[7];
            Integer numberSatellites = Integer.parseInt(numberSatellitesString);
            if(fixQuality > 0 && fixQuality < 5 && numberSatellites > 4){
                GGAGood = true;
            } else{
                GGAGood = false;
            }
        }
        if(GGAGood) {
            if (rawNmeaSplit[0].equalsIgnoreCase("$GPGSA")) {
                String PDOPString = rawNmeaSplit[15];
                Double PDOP = Double.parseDouble(PDOPString);
                if (PDOP < 6) {
                    GSAGood = true;
                } else {
                    GSAGood = false;
                }

            }
            if (GSAGood) {
                for (int i = 0; i < 4; i++) {
                    if (rawNmeaSplit[0].equalsIgnoreCase("$GPGSV")) {
                        String SNRString = rawNmeaSplit[0];
                        Integer SNR = Integer.parseInt(SNRString);
                        if (SNR > 30) {
                            GSVGood = true;
                        }
                    }
                }
            }
        }
        if(GGAGood && GSAGood && GSVGood){
            gpsQuality = true;
        }
    }
}
