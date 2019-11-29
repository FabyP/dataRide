package com.example.dataride;

import android.Manifest;
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
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
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

    LocationManager locationManager = null;
    OnNmeaMessageListener nmeaListener = null;
    private Handler mHandler = null;
    LocationListener gpsListener = null;
    Location loc1 = null;
    Location loc2 = null;


    boolean goodGps = false;

    //Variablen für Berechnung
    double long1;
    double long2;
    double lat;
    double latRad;
    double lat1;
    double lat2;
    double dx;
    double dy;

    double distance;
    double distanceSpeed;

    double speed;
    double speedDefault;
    double time;
    double overallTime;

    //co2 Berechnung Variablen
    double gas;
    double coOutput;
    String gasType;
    double gasAmount;
    double driven;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                locationManager =
                        (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                //Abfrage ob Gps an ist, ist das doppelt zu der methode weiter unten?
                //in die methode des on click start buttons einfügen sobald vorhanden
                //Position abfragen
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return;
                }
                    locationManager.addNmeaListener(nmeaListener);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_TO_REFRESH,
                            MIN_DISTANCE_TO_REFRESH,
                            MainActivity.this);
                    loc1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(loc1 != null) {
                        long1 = loc1.getLongitude();
                        lat1 = loc1.getLatitude();
                    } else{
                    // müsste automatisch das Signal einschalten sobald man bestätigt
                    Toast.makeText(MainActivity.this, "GPS nicht gefunden", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);

                }
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {

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

    }
}
