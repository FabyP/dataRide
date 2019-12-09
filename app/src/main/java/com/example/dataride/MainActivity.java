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


public class MainActivity extends AppCompatActivity implements LocationListener, OnNmeaMessageListener {
    // minimale zeit und distanz ab der die gps daten aktualisiert werden
    //angegeben in milli sekunden
    // momentan jede sekunde, ist das ausreichend?
    private static final long MIN_TIME_TO_REFRESH = 1000L;
    //angegeben in meter
    //hier auf 0 gesetzt damit er einfach immer nur das zeitintervall nimmt
    private static final float MIN_DISTANCE_TO_REFRESH = 0F;

    LocationManager lm = null;
    OnNmeaMessageListener nmeaListener = null;
    LocationListener gpsListener = null;

    //Testzwecken
    TextView textLong;
    TextView textLat;
    TextView textNmea;
    TextView text4;

    FloatingActionButton fab;
    boolean clicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        textLat = (TextView) findViewById(R.id.textView);
        textLong = (TextView) findViewById(R.id.textView2);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        clicked = false;


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
                if(clicked){
                    Toast.makeText(MainActivity.this,"Stop Tracking",Toast.LENGTH_SHORT).show();
                    Log.v("CLICKED","");
                    stopTracking();
                    clicked = false;
                } else{
                    startTracking();
                    Toast.makeText(MainActivity.this,"Start Tracking",Toast.LENGTH_SHORT).show();
                    Log.v("CLICKED","");
                    clicked = true;
                }

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
            lm.addNmeaListener(MainActivity.this);
            boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled) {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 6000, 0, MainActivity.this);
            } else {
                textLong.setText("SHIT");
            }
        } else{
            Toast.makeText(MainActivity.this, "Kein Signal gefunden", Toast.LENGTH_LONG).show();
        }
    }
    public  void stopTracking(){
        //hier sicherstellen das er alles vorherige rausnimmt
        textLat.setText("Remove");
        textLong.setText("Remove");
    }

    //Android Lifecycle Methoden
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        lm.removeUpdates(MainActivity.this);
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
        lm.removeUpdates(MainActivity.this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        String latString = Double.toString(lat);
        String lonString = Double.toString(lon);
        textLat.setText(latString);
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

    @Override
    public void onNmeaMessage(String message, long timestamp) {
        textLong.setText(message);
    }
}
