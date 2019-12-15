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
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;


@RequiresApi(api = Build.VERSION_CODES.N)
public class MainActivity extends AppCompatActivity implements LocationListener, OnNmeaMessageListener {

    // minimale zeit und distanz ab der die gps daten aktualisiert werden
    //angegeben in milli sekunden
    private static final long MIN_TIME_TO_REFRESH = 3000L;

    //angegeben in meter
    //hier auf 0 gesetzt damit er einfach immer nur das zeitintervall nimmt
    private static final float MIN_DISTANCE_TO_REFRESH = 0F;

    LocationManager lm = null;


    //Testzwecken
    TextView textLong;
    TextView textLat;

    //Variablen für FABoulos
    FloatingActionButton fab;
    boolean clicked;

    //Variable für Filter
    boolean gpsQuality;

    //Variablen zur Berechnung
    double Latitude1;
    double Longtitude1 ;
    double Latitude2 ;
    double Longtitude2;

    double distance;
    double totalDistance;
    double distanceTime;
    double savedTime;

    double speed;
    double defaultSpeed;


    Button b_settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        b_settings = (Button) findViewById(R.id.b_settings);

        b_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        BottomNavigationView navView = findViewById(R.id.nav_view);
        textLat = (TextView) findViewById(R.id.textView);
        textLong = (TextView) findViewById(R.id.textView2);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        clicked = false;

        gpsQuality = false;

        defaultSpeed = 130;


        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_car, R.id.navigation_navigation, R.id.navigation_statistics)
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
                    stopTracking();
                    clicked = false;
                } else{
                    startTracking();
                    Toast.makeText(MainActivity.this,"Start Tracking",Toast.LENGTH_SHORT).show();
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
        textLat.setText("Remove");
        textLong.setText("Remove");
        //lm.removeUpdates(MainActivity.this);
        //lm.removeNmeaListener(MainActivity.this);
    }

    @Override
    public void onLocationChanged(Location location) {}

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
        getLatLong(message);

        //Daten abspeichern
        //String time = Long.toString(timestamp);
        //writeRawDataInStorage(MainActivity.this, time, message);

        //Daten filtern und berechnen
        //filterNmea(message);

        /*if(gpsQuality){
            getLatLong(message);

            //checken ob null ist
            //nicht sicher ob das so funktioniert
            //noch Probleme damit zwei Positionen zu haben bevor Berechnung beginnt
            if(Latitude2 == 0.0 ){
                changeAttributes();
                } else {
                getSpeed(message);
                distance = +distance(Latitude1, Longtitude1, Latitude2, Longtitude2);
                totalDistance = +distance;

                if (speed > defaultSpeed) {
                    distanceTime = +distance;
                    savedTime = +savedTime();
                }
                changeAttributes();
            }

            } else{
                return;
            }*/

    }


    public void filterNmea(String nmea){
        boolean GSAGood = false;
        boolean GGAGood = false;
        boolean GSVGood = false;
        String[] rawNmeaSplit = nmea.split(",");

        GGAGood = filterGGA(rawNmeaSplit);
        GSAGood = filterGSA(rawNmeaSplit);
        GSVGood = filterGSV(rawNmeaSplit);
        if (GSAGood && GGAGood && GSVGood){
            gpsQuality = true;
        } else{
            gpsQuality = false;
        }

    }


    public boolean filterGGA(String[] rawNmeaSplit){
        boolean gga = false;

        if (rawNmeaSplit[0].equalsIgnoreCase("$GPGGA")){
            String fixQualityString = rawNmeaSplit[6];
            Integer fixQuality = Integer.parseInt(fixQualityString);
            String numberSatellitesString = rawNmeaSplit[7];
            Integer numberSatellites = Integer.parseInt(numberSatellitesString);
            if(fixQuality > 0 && fixQuality < 5 && numberSatellites > 4){
                gga = true;
            } else{
                gga =  false;
            }
        }
        return gga;
    }

    public boolean filterGSA(String[] rawNmeaSplit){
            boolean gsa = false;
            if(rawNmeaSplit[0].equalsIgnoreCase("$GPGSA")){
                String PDOPString=rawNmeaSplit[15];
                Double PDOP=Double.parseDouble(PDOPString);
                if(PDOP< 6){
                    gsa = true;
                }else{
                    gsa = false;
                }
            }
            return gsa;
    }

    //Vergleich funktioniert nicht, noch testen
    public boolean filterGSV(String[] rawNmeaSplit){
        boolean gsv = false;
        Integer n = 0;
        Integer m = 4;
            for (int i = 0; i < 4; i++) {
                if (rawNmeaSplit[0].equalsIgnoreCase("$GPGSV")) {
                    String SNRString = rawNmeaSplit[0];
                    Integer SNR = Integer.parseInt(SNRString);
                    if (SNR > 30) {
                        n = +1;
                    }
                }
            }
            if(n.intValue() == m.intValue()){
                gsv = true;
            } else{
                gsv = false;
            }

        return gsv;
    }

    public void getSpeed(String nmea) {
        String[] rawNmeaSplit = nmea.split(",");

        if (rawNmeaSplit[0].equalsIgnoreCase("$GPVTG")) {
            speed = Double.parseDouble(rawNmeaSplit[7]);
        }
    }

    public void getLatLong(String nmea) {
        String[] rawNmeaSplit = nmea.split(",");

        if (rawNmeaSplit[0].equalsIgnoreCase("$GPGGA")) {
            Latitude1 = Double.parseDouble(rawNmeaSplit[2]);
            Longtitude1 = Double.parseDouble(rawNmeaSplit[4]);
            textLat.setText(String.valueOf(Latitude1));
            textLong.setText(String.valueOf(Longtitude1));
        }
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double d = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        double dis = 6378.388 * Math.acos(d);
        return dis;
    }

    private double deg2rad(double deg) {

        return (deg * Math.PI / 180.0);
    }

    //Mehtode funktioniert so nicht, convert erwartet anderes Format muss ich selbst converter schreiben
    private double convertDecimal(double degree){
        String converted = Location.convert( degree , Location.FORMAT_DEGREES);
        return Double.parseDouble(converted);
    }

    public double savedTime(){
        double time = distance/defaultSpeed;
        return time;
    }

    public void changeAttributes(){
        Latitude1 = Latitude2;
        Longtitude1 = Longtitude2;
    }

// kann eingefügt werden sobald Felder für Sprit und Sprittyp vorhanden sind
/*    public void coOutput(){
        gasType =  gasTypeApp.getText().toString();
        gasAmount = Double.parseDouble(gasAmountApp.getText().toString());
        switch(gasType) {
            case "Benzin":
                gas = 3.24;
                break;
            case "Autogas":
                gas = 1.90;
                break;
            case "Diesel":
            default:
                gas = 2.88;
                break;
        }
        if( gas != 0.00){
            coOutput = gas * (driven / 100 * gasAmount);
            coAmount.setText(coOutput);
        } else{
            //Angabe das keine gemacht wurde und das co2 feld wird leer angezeigt?!
            //enable des Buttons?
        }

    }*/

//muss noch getestet werden ob das funktioniert
    public void writeRawDataInStorage(Context mcoContext, String fileName, String sBody){
        File path = new File(mcoContext.getExternalFilesDir(null),"GPSDATA");
        BufferedWriter bw = null;
        if(!path.exists()){
            path.mkdir();
        }

        try{
            File gpsfile = new File(path, fileName);

            if(!gpsfile.exists()){
                gpsfile.createNewFile();
            }

            FileWriter fw = new FileWriter(gpsfile, true);
            bw = new BufferedWriter(fw);
            bw.append(sBody);
            bw.newLine();

        } catch (Exception e){
            e.printStackTrace();
        }
        finally
        {
            try{
                if(bw!=null)
                    bw.close();
            }catch(Exception ex){
                System.out.println("Error in closing the BufferedWriter"+ex);
            }
        }
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
        lm.removeNmeaListener(MainActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //hier wird alles entfernt sobald der Nutzer die app schließt
    @Override
    protected void onDestroy() {
        super.onDestroy();
        lm.removeUpdates(MainActivity.this);
        lm.removeNmeaListener(MainActivity.this);
    }
}

//Test Berechnung
/*public void test(){
        float results[] =  new float[1];
        double latitude1 = 50.584444;
        double longtitude1 = 8.671053;
        double latitude2 = 50.587444;
        double longtitude2 = 8.669015;
        Location.distanceBetween(latitude1, longtitude1, latitude2, longtitude2, results);
        String a = String.valueOf(results[0]);
        textNmea.setText(a);
        double distance = distance(latitude1, longtitude1, latitude2, longtitude2);
        text4.setText(String.valueOf(distance));
        double distanceKugel = distanceKugel(latitude1, longtitude1, latitude2, longtitude2);
        textLat.setText(String.valueOf(distanceKugel));
    }*/
/*    public double distance(double lat1, double long1, double lat2, double long2){
        double lat = (lat1 + lat2)/2*0.01745;
        double dx = 111.3*(Math.cos(deg2rad(lat)))*(long1-long2); // zurück in Grad rechnen
        double dy = 111.3*(lat1-lat2);

        double distance1 = Math.sqrt(dx*dx+dy*dy);
        return distance1;
    }*/