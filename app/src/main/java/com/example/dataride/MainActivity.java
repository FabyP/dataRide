package com.example.dataride;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.OnNmeaMessageListener;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dataride.ui.car.CarViewModel;
import com.example.dataride.ui.statistics.InfoDialog;
import com.example.dataride.ui.statistics.StatisticsViewModel;
import com.example.dataride.ui.statistics.StatisticsFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.text.DecimalFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static com.example.dataride.R.id.currentSpeed;
import static com.example.dataride.R.id.info;

@RequiresApi(api = Build.VERSION_CODES.N)
public class MainActivity extends AppCompatActivity implements LocationListener, OnNmeaMessageListener{


    //ViewModel
    private CarViewModel model;
    private StatisticsViewModel statisticsModel;

    //Preferences
    public static final String PREF_GAS_VAR = "pref_gas";
    public static final String PREF_AVERAGE_FUEL_CONSUMPTION_VAR = "pref_average_fuel_consumption";
    public static final String PREF_SPEED_LIMIT_VAR = "pref_speed_limit";

    // minimale zeit und distanz ab der die gps daten aktualisiert werden
    //angegeben in milli sekunden
    private static final long MIN_TIME_TO_REFRESH = 1000L;
    //angegeben in meter
    //hier auf 0 gesetzt damit er einfach immer nur das zeitintervall nimmt
    private static final float MIN_DISTANCE_TO_REFRESH = 0F;

    LocationManager lm = null;

    //Variablen für fab
    FloatingActionButton fab;
    boolean clicked;

    //Variable für Filter
    boolean gsa, gga, GSAGood;

    //Variablen zur Berechnung
    double LatitudeFirst;
    double LongtitudeFirst;
    double LatitudeLast;
    double LongtitudeLast;
    double distance;
    double totalDistance;
    double distanceTime;
    double savedTime;
    String savedTimeString;
    //long totalTime;
    LiveData<String> totalTime;
    long startTime = 0;
    long endTime;
    double a, b, c;
    double speed;
    double defaultSpeed;
    String speedText;

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable(){

        @Override
        public void run() {
            model = ViewModelProviders.of(MainActivity.this).get(CarViewModel.class);
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            //textLong.setText(String.format("%d:%02d", minutes, seconds));
            model.setPassedTime(String.format("%d:%02d", minutes, seconds));

            //Final formatierter Text
            //model.setPassedTime(String.format("%d", minutes) + " min");

            timerHandler.postDelayed(this, 500);
        }
    };


    //benötigt um Daten so auszugeben das sie für den User in gut angezeigt werden
    DecimalFormat df = new DecimalFormat("#0.0");
    DecimalFormat dfTime = new DecimalFormat("#0.00");

    //zum schreiben der Datein benötigt
    String nmeaData;
    String valuesData;
    StringBuilder sb;
    StringBuilder sta;
    File myFile;

    Button b_settings;
    private String Tag;

    //benötigt um Spritverbrauch zu berechnen
    String gasType;
    double averageGasAmount;
    double gasAmount;
    double gas;
    double coOutput;



    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*b_settings = (Button) findViewById(R.id.b_settings);

        b_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });*/

        BottomNavigationView navView = findViewById(R.id.nav_view);
        //textLat = (TextView) findViewById(R.id.textView2);
        //textLong = (TextView) findViewById(R.id.textView);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_play_arrow_24dp);

        clicked = false;

        LatitudeLast = 0.0;
        LongtitudeLast = 0.0;

        //initialisiert die String Builder um diese dann an das File zu übergeben
        sb = new StringBuilder();
        sta = new StringBuilder();

        //initialisiert das File indem die Daten für die Statistik Ausgabe geschrieben werdne
        myFile = new File(getExternalFilesDir(null), "values");


        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_car, R.id.navigation_navigation, R.id.navigation_statistics)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        //Achtet darauf wenn der Button geklickt wird
        //passiert mit Hilfe einer boolean Variable die jeweils auf True oder False gebracht wird
       fab.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                if(clicked){
                    //setzt das Bild für den Button
                    fab.setImageResource(R.drawable.ic_play_arrow_24dp);
                    //gibt nochmal eine Pop-Up Meldung aus damit User wirklich sicher sein kann den Button geklickt zu haben
                    Toast.makeText(MainActivity.this,"Stop Tracking",Toast.LENGTH_SHORT).show();



                    stopTracking();

                    //speichert die Zeit ab wenn auf Stop gedrückt wird
                    endTime = System.currentTimeMillis();

                    timerHandler.removeCallbacks(timerRunnable);

                    //ändert die Variable in false damit beim nächsten Button klick das andere Event gestartet wird
                    clicked = false;
                } else{
                    //setzt das Bild für den Button
                    fab.setImageResource(R.drawable.ic_pause_black_24dp);

                    //nimmt die Zeit auf in der Auf start gedrückt wurde
                    startTime = System.currentTimeMillis();
                    timerHandler.postDelayed(timerRunnable, 0);

                    model = ViewModelProviders.of(MainActivity.this).get(CarViewModel.class);
                    savedTimeString = dfTime.format(savedTime);
                    model.setSavedTime(savedTimeString);

                    //startet das Aufnehmen der Daten
                    startTracking();

                    //gibt nochmal eine Pop-Up Meldung aus damit User wirklich sicher sein kann den Button geklickt zu haben
                    Toast.makeText(MainActivity.this,"Start Tracking",Toast.LENGTH_SHORT).show();

                    //ändert die Variable in false damit beim nächsten Button klick das andere Event gestartet wird
                    clicked = true;
                }
            }
        });

    }


    //Notwendig da sonst der NMEA-Listener nicht gehen würde
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void startTracking(){

        //holt sich die eingegeben Daten für den Spritverbrauch und den DeafultSpeed
        gasType = readGasSettings();
        averageGasAmount = readFuelSettings();
        defaultSpeed = readSpeedLimitSettings();

        //überprüft ob die Vorraussetzungen gegeben sind damit das Aufnehmen gestartet werden kann
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET
            }, 10);
            return;
        }

        //speichert den LocationManager
        lm = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);

        //überprüft die letzte Vorrausetzung ob die Aufnahme gestartet werden kann
        if(!lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER )) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        if(lm != null) {

                //wenn beim erstellen des Location Manager kein Fehler aufgetreten ist startet er das abrufen der GPS-Daten
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_TO_REFRESH, MIN_DISTANCE_TO_REFRESH, MainActivity.this);

                //guckt ob bereits schon eine letzte Position vorhanden war
                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                //fügt den Nmea Listener hinzu
                lm.addNmeaListener(MainActivity.this);
        } else{
            Toast.makeText(MainActivity.this, "Es ist ein Fehler mit der GPS-Verbingung aufgetreten.", Toast.LENGTH_LONG).show();
        }

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_TO_REFRESH, MIN_DISTANCE_TO_REFRESH, MainActivity.this);
        lm.addNmeaListener(MainActivity.this);




    }

    //entfernt alle Listener und zeigt dem Nutzer an das das Tracking gestoppt wurde
    public  void stopTracking() {
        model = ViewModelProviders.of(this).get(CarViewModel.class);

        //holt sich die berechneten Werte des Sprit- und CO2-Verbrauches
        coOutput();

        //fügt an den String Builder alle relevanten Daten an
        sb.append("insgesamt gesparte Zeit " + savedTime + " Minuten " + "\n");
        sb.append(" Gesamtdistanz " + totalDistance + " km " + "\n");
        sb.append(" Distanz, die umgerechnet wurde " + distanceTime + " km " + "\n");
        sb.append(" CO2-Output " + coOutput + "\n");
        sb.append(" Spirtverbrauch " + gasAmount + " Liter " + "\n");

        //holt sich ein aktuelles Datum das für die Statistik benötigt wird
        Calendar kalender = Calendar.getInstance();
        SimpleDateFormat datumsformat = new SimpleDateFormat("dd.MM.yyyy");
        String stamp = datumsformat.format(kalender.getTime());

        //schreibt alle für die Statistik relevanten Daten in den dafür vorgsehenen String Builder
        sta.append(stamp + ";" + savedTime + ";" + gasAmount + ";" + coOutput + "#");

        //schreibt beide StringBuilder in files
        writeFileExternalStorage(sb);
        writeToStatistics(sta);

        model.setSpeed("0,0");

        lm.removeUpdates(MainActivity.this);
        lm.removeNmeaListener(MainActivity.this);

        resetValues();
    }

    public void resetValues(){
        savedTime = 0;
        totalDistance = 0;
        coOutput = 0;
        gasAmount = 0;
        LatitudeLast = 0;
        LongtitudeLast = 0;
    }

    @Override
    public void onNmeaMessage(String message, long timestamp) {

        //holt sich den Längen und Breitengrad
        getLatLong(message);
        //holt sich den Speed
        getSpeed(message);

        //filtert den PDOP Wert
        //GSAGood = filterGSA(message);

        if(speed >= 1) {
            /*if (GSAGood) {
                gsa = true;
            } else {
                gsa = false;
            }*/

            String[] rawNmeaSplit = message.split(",");

            //filtert die FixQuality und die Satellitenanzahl
            float fixQuality;
            float numberSatellites;

            /*if (rawNmeaSplit[0].equalsIgnoreCase("$GPGGA")) {

                String fixQualityString = rawNmeaSplit[6];
                String numberSatellitesString = rawNmeaSplit[7];

                if(fixQualityString.length() == 0) {
                    gga = false;
                }else if(numberSatellitesString.length() == 0){
                    gga = false;
                } else {

                    fixQuality = Float.parseFloat(fixQualityString);
                    numberSatellites = Float.parseFloat(numberSatellitesString);

                    if(fixQuality > 0 && fixQuality < 5 && numberSatellites > 4){
                        gga = true;

                    } else{
                        gga = false;

                    }
                }
            }*/

            /*if (gsa) {
                if (gga) {
                    //scheibt in den Stringbuilder die Längen und Breitengrade
                    sb.append("Lat1;" + LatitudeFirst + ";" + "Long1;" + LongtitudeFirst + "\n");
                    startMath();
                }
            }*/
            sb.append("Lat1;" + LatitudeFirst + ";" + "Long1;" + LongtitudeFirst + "\n");
            startMath();
        }

    }


    public void startMath(){
        model = ViewModelProviders.of(this).get(CarViewModel.class);
        statisticsModel = ViewModelProviders.of(this).get(StatisticsViewModel.class);

        //sollte der 2 Längengrad noch leer sein so ruf die Methode auf und berechne noch nichts
        //dies ist der Fall bei einem ersten Signal, da hier noch keine zweite Position vorhanden ist
        if(LatitudeLast == 0.0 ){
            //wechselt LängengradFirst zu Last usw.
            changeAttributes();
        } else {
            //berechnet die gefahrene Distanz
            distance = distance(LatitudeFirst, LongtitudeFirst, LatitudeLast, LongtitudeLast);
            Double distanceCheck = distance;
            //checkt ob die Distanz ungleich NaN ist
            if(!distanceCheck.isNaN()){
                //zählt die Gesamtdistanz hoch
                totalDistance = totalDistance + distance;

                //ab hier werden erst die Berechnungen gestartet sollte der Speed über dem Defaultspeed sein
                if (speed >= defaultSpeed) {
                    //zählt die Distanz hoch die über der Geschwindigkeit gefahren wurde
                    distanceTime = distanceTime + distance;


                    //erechnet die Differenz zwischen den beiden gerbauchten Zeiten
                    b = savedTime(distance, speed);
                    a = savedTime(distance, defaultSpeed);
                    c = a-b;

                    //zählt die gesparte Zeit hoch
                    savedTime = savedTime + c;

                    //damit es in der Anzeige eine sinnvolle Darstellung hat wird es geeignet angepasst
                    savedTimeString = dfTime.format(savedTime);
                    model.setSavedTime(savedTimeString);
                    //statisticsModel.setSavedTimeStat(savedTimeString);
                }
            }
            //wechselt LängengradFirst zu Last usw.
            changeAttributes();
        }
    }


    //Methode überprüft innerhalb des GSA: PDOP
    //PDOP < 6
    public boolean filterGSA(String nmea){
        boolean gsa = false;
        String PDOPString;
        String[] rawNmeaSplit = nmea.split(",");
        if(rawNmeaSplit[0].equalsIgnoreCase("$GPGSA")) {

            PDOPString = rawNmeaSplit[23];

            if(PDOPString.length() == 0){

                gsa = false;
            } else {
                Double PDOP=Double.parseDouble(PDOPString);

                if(PDOP< 6){
                    gsa = true;

                }else{
                    gsa = false;

                }
            }
        }
        return gsa;
    }


    //Methode überprüft innerhalb des GSV: SNR
    //SNR > 30
    //Filter Aufgrund von Testdaten NICHT VERWENDET
/*    public boolean filterGSV(String nmea){
        boolean gsv = false;
        double SNR;
        String[] rawNmeaSplit = nmea.split(",");
        if (rawNmeaSplit[0].equalsIgnoreCase("$GPGSV")) {
            String SNRString = rawNmeaSplit[7];
            if(SNRString.length() == 0){
                gsv = false;
                sb.append("SNR FAILED " + nmea);
            } else {
                SNR = Double.parseDouble(SNRString);
                sb.append("SNR " + SNRString);
                if (SNR >= 20) {
                    gsv = true;
                    sb.append("SNR " + gsv);
                } else {
                    gsv = false;
                    sb.append("SNR " + gsv);
                }
            }
        }
        return gsv;
    }*/



    //gibt die Geschwindigkeit aus
    //einmal als double für die Berechnungen
    //einmal als String um die Geschwindigkeit im Layout anzuzeigen
    public void getSpeed(String nmea) {
        String[] rawNmeaSplit = nmea.split(",");
        if (rawNmeaSplit[0].equalsIgnoreCase("$GPRMC")) {

            model = ViewModelProviders.of(this).get(CarViewModel.class);

            speed = Double.parseDouble(rawNmeaSplit[7]);
            //Konvertiert die Knoten in km/h
            speed = convertKMH(speed);
            //Wandel den Text bereit zur Ausgabe so um das er gut lesbar ist, notwendig da sonst endlos viele Kommastellen
            speedText = df.format(speed);
            model.setSpeed(speedText);
        }
    }

    //speichert Längen und Breitengrad und gibt es an eine setText Methode weiter
    public void getLatLong(String nmea) {
        String[] rawNmeaSplit = nmea.split(",");

        if (rawNmeaSplit[0].equalsIgnoreCase("$GPGGA")) {

            String Lati = rawNmeaSplit[2];

            //Zahlen auf Konvertierung und Ausgabe vorbereiten
            String Number1 = Lati.substring(0, 2);
            String Number2 = Lati.substring(2, Lati.length());
            double Number1Double = Double.parseDouble(Number1);
            double Number2Double = Double.parseDouble(Number2);

            LatitudeFirst = convertDecimal(Number1Double, Number2Double);

            String Longi = rawNmeaSplit[4];

            //Zahlen auf Konvertierung und Ausgabe vorbereiten
            String Number3 = Longi.substring(0, 3);
            String Number4 = Longi.substring(3, Longi.length());
            double Number3Double = Double.parseDouble(Number3);
            double Number4Double = Double.parseDouble(Number4);

            LongtitudeFirst = convertDecimal(Number3Double, Number4Double);

            //wird übergeben damit Text ausgegeben wird
            //wird in der aktuellen Version nicht mehr verwendet
            //setText(Number1, Number2, Number3, Number4);
        }
    }
        //wird in der aktuellen Version nicht mehr verwendet
/*    //setzt den Text vo Längen- und Breitengrad
    public void setText(String lat1, String lat2, String long1, String long2){
        //wandelt die Angaben so um das sie für den Nutzer gut erkennbar sind
        textLat.setText(lat1 + (char) 0x00B0 + " " + lat2 + "\"");
        textLong.setText(long1 + (char) 0x00B0 + " " + long2 + "\"");
    }*/

    //Berechnet die Distanz zwischen zwei GPS-Angaben
    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double d = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        double dis = 6378.388 * Math.acos(d);
        return dis;
    }

    //wandelt Winkel in Rad um
    private double deg2rad(double deg) {

        return (deg * Math.PI / 180.0);
    }

    //Konvertiert GPS-Angaben aus NMEA in Dezimaldarstellung
    private double convertDecimal(double degree, double minute){
        double converted = degree + (minute/60);
        return converted;
    }

    //Konvertiert GPS-Angaben aus NMEA in km/h um
    private double convertKMH(double knot){
        if (knot >= 1){
            return knot/0.53996;
        } else{
           return 0.00;
        }
    }

    //berechnet die Zeit aus die mit der wirklich gefahrenen verechnet wird
    //rechnet die Zeit aus die mit der Defaultspeed für die Strecke nötig gewesen wäre
    public double savedTime(double length, double velocity){
        double time;

        if(velocity < 1){
            time = 0;

        } else if(length == 0){
            time = 0;

        } else{
            time = (length/velocity)*60;
        }
        return time;
    }

    //wechselt die Attribute
    public void changeAttributes(){
        LatitudeLast = LatitudeFirst;
        LongtitudeLast = LongtitudeFirst;
    }

    //schreibt den StringBuilder in ein File
    public void writeFileExternalStorage(StringBuilder a) {

        nmeaData = String.valueOf(a);
        String fileName = "dataRide " + System.currentTimeMillis();

        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {

            return;
        }


        File file = new File(getExternalFilesDir(null), fileName);

        FileOutputStream outputStream = null;
        try {
            file.createNewFile();
            outputStream = new FileOutputStream(file, true);

            outputStream.write(nmeaData.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //schreibt den Statistik StringBuilder in ein File das bestimmt für das Statistik-Fragment ist
    public void writeToStatistics(StringBuilder a) {

        valuesData = String.valueOf(a);


        FileOutputStream outputStream = null;
        try {
            myFile.createNewFile();

            outputStream = new FileOutputStream(myFile, true);

            outputStream.write(valuesData.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


   //berechnet anhand der Nutzereingaben die vorgesehenen Richtwerte für Spritverbrauch und CO2vebrauch
   public void coOutput(){
        //holt sich den eingegeben Kraftstofftyp
        switch(gasType) {
            case "Benzin":
                gas = 2.32;
                break;
            case "Diesel":
                gas = 2.65;
            default:
                break;
        }
        if( gas >= 1){
            if(totalDistance >= 1) {
                //berechnet den gesamten Spritverbrauch
                gasAmount = (averageGasAmount/100) * totalDistance;
                //berechnet den CO2verbrauch
                coOutput = gas * gasAmount;

                //setzt die Werte in sinnvolle anzeigbare Werte um
                String gasAmountString = dfTime.format(gasAmount);
                String coOutputString = dfTime.format(coOutput);

                //gibt die an das Statistik-Fragment weiter
                /*statisticsModel = ViewModelProviders.of(this).get(StatisticsViewModel.class);
                statisticsModel.setGasAmountStat(gasAmountString);
                statisticsModel.setCo2Stat(coOutputString);*/
            }
        } else{
            gasAmount = 0.0;
            coOutput = 0.0;
            /*statisticsModel = ViewModelProviders.of(this).get(StatisticsViewModel.class);
            statisticsModel.setGasAmountStat(String.valueOf(gasAmount));
            statisticsModel.setCo2Stat(String.valueOf(coOutput));*/
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
    }

    //Alle Rechte werden an dieser Stelle abgefragt
    //Rechte auf: Speicherzugriff, Standortangaben
    @Override
    protected void onResume() {
        super.onResume();
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET
            }, 10);
            return;
        }
        lm = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
        if(!lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER )) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }



    //hier wird alles entfernt sobald der Nutzer die app schließt
    @Override
    protected void onDestroy() {
        super.onDestroy();
        lm.removeUpdates(MainActivity.this);
        lm.removeNmeaListener(MainActivity.this);
        timerHandler.removeCallbacks(timerRunnable);
    }


    //Read Settings
    public String readGasSettings(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String gasPref = sharedPreferences.getString("pref_gas", "");

        return gasPref;
    }

    public double readFuelSettings(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String averageFuelConsumptionPref = sharedPreferences.getString("pref_average_fuel_consumption", "");
        averageFuelConsumptionPref = averageFuelConsumptionPref.replace(",", ".");
        double averageFuel = Double.parseDouble(averageFuelConsumptionPref);

        return averageFuel;
    }

    public double readSpeedLimitSettings(){
        double speedLimit = 0;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String speedLimitPref = sharedPreferences.getString("pref_speed_limit", "");
        speedLimitPref = speedLimitPref.replace(",", ".");
        speedLimit = Double.parseDouble(speedLimitPref);
        return speedLimit;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.example_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){
            case R.id.item1:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

                default:
                    return super.onOptionsItemSelected(item);
        }
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

}
