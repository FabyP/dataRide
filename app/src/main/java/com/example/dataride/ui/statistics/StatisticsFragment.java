package com.example.dataride.ui.statistics;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.dataride.MainActivity;
import com.example.dataride.R;

import org.honorato.multistatetogglebutton.MultiStateToggleButton;
import org.honorato.multistatetogglebutton.ToggleButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Objects;

import static android.content.ContentValues.TAG;

public class StatisticsFragment extends Fragment {

    private String test;

    DecimalFormat df = new DecimalFormat("#0.0");
    DecimalFormat dfTime = new DecimalFormat("#0");

    private TextView savedTimeStatText;
    private TextView co2StatText;
    private TextView gasAmountStatText;
    private ImageButton infoButton;
    private MultiStateToggleButton button;

    //Werte die für die Statistikausgabe anhand der Buttons benötigt werden
    double savedTimePrint;
    double gasAmountPrint;
    double co2AmountPrint;
    //String line;
    StringBuilder text = new StringBuilder();

    FileInputStream file;
    BufferedReader br;
    final File data = new File("/sdcard/Android/data/com.example.dataride/files/values");


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        StatisticsViewModel statisticsViewModel = ViewModelProviders.of(this).get(StatisticsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_statistics, container, false);
        final TextView textView = root.findViewById(R.id.text_savings);
        statisticsViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        this.savedTimeStatText = root.findViewById(R.id.timeSavings_text);
        this.co2StatText = root.findViewById(R.id.co2_text);
        this.gasAmountStatText = root.findViewById(R.id.gasAmount_value);

        infoButton = root.findViewById(R.id.infoButton);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        button = (MultiStateToggleButton) root.findViewById(R.id.mstb_date_range);

        button.setOnValueChangedListener(new ToggleButton.OnValueChangedListener() {
            @Override
            public void onValueChanged(int position) {

                savedTimePrint = 0;
                gasAmountPrint = 0;
                co2AmountPrint = 0;

                //Log.d(TAG, "Position: " + position);

                if (data.exists()) {

                    try {
                        String line;
                        file = new FileInputStream(data);
                        br = new BufferedReader(new InputStreamReader(file));
                        if ((line = br.readLine()) != null) {
                            test = line;
                            /*Toast.makeText(getActivity(), test,
                                    Toast.LENGTH_LONG).show();*/
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }


                //DAS HIER MUSS ERSETZT WERDEN DA VALUE EIGENTLICH VON DEN BUTTON KOMMEN MÜSSTE MIT DER ANGABE BEI WELCHEM STRING MAN IST OB TAG MONAT...
                String value = "";


                //holt sich das aktuelle Datum und schreibt es in Stamp
                Calendar kalender = Calendar.getInstance();
                SimpleDateFormat datumsformat = new SimpleDateFormat("dd.MM.yyyy");
                String stamp = datumsformat.format(kalender.getTime());

                //BufferedReader br = null;

                /*try {
                    br = new BufferedReader(new FileReader(file));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }*/
                //hängt an Text den gesamten Inhalt von Values
                /*while (true) {
                    try {
                        if ((line = br.readLine()) == null) break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    text.append(line);
                }*/

                //wenn Tag wird hier verarbeitet
                if (position == 0) {
                    //macht den String Builder in einen String
                    String message = test;
                    //splitetet den String immer am Hashtag auf
                    String[] rawValuesSplit = message.split("#");
                    //holt sich die Länge des Arrays um iterieren zu können
                    int size = rawValuesSplit.length;
                    for (int i = 0; i < size; i++) {
                        //iteriert über jedes Arrayitem drüber und macht dafür das folgende
                        String rawValues = rawValuesSplit[i];
                        //splitet den gegeben Satz nochmal auf am ; um alle Werte einzeln zu haben: Datum, SavedTime, Gas, CO2
                        String[] values = rawValues.split(";");
                        //wenn das Datum im ersten dem Stamp gleicht war es der selbe Tag
                        if (values[0].equals(stamp)) {
                            //zählt die Werte auf die passenden drauf
                            savedTimePrint = savedTimePrint + Double.parseDouble(values[1]);
                            gasAmountPrint = gasAmountPrint + Double.parseDouble(values[2]);
                            co2AmountPrint = co2AmountPrint + Double.parseDouble(values[3]);
                        }

                    }
                    String savedTimeString = dfTime.format(savedTimePrint);
                    String gasAmountString = df.format(gasAmountPrint);
                    String coOutputString = df.format(co2AmountPrint);
                    //setzt dementsprechnend die Werte neu in der Ansicht die für den Nutzer sichtbar ist
                    savedTimeStatText.setText(savedTimeString + " min");
                    co2StatText.setText(coOutputString + " kg");
                    gasAmountStatText.setText(gasAmountString + " l");
                } else if (position == 1) {
                    //macht den String Builder in einen String
                    String message = test;
                    //splitetet den String immer am Hashtag auf
                    String[] rawValuesSplit = message.split("#");
                    //holt sich die Länge des Arrays um iterieren zu können
                    int size = rawValuesSplit.length;
                    //splitet den Stamp auf damit wir davon den Monat bestimmen können
                    String[] rawDate = stamp.split("\\.");
                    //speichert den Monat ab
                    String month = rawDate[1];
                    for (int i = 0; i < size; i++) {
                        String rawValues = rawValuesSplit[i];
                        String[] values = rawValues.split(";");
                        //speichert das Datum zum aufsplitten
                        String a = values[0];
                        //splittet das geholte Datum auf damit der Monat geholt werden kann
                        String[] rawDateValues = a.split("\\.");
                        //wenn der Monat dem StampMonat gleicht war es der selbe Monat
                        if (rawDateValues[1].equals(month)) {
                            //zählt die Werte auf die passenden drauf
                            savedTimePrint = savedTimePrint + Double.parseDouble(values[1]);
                            gasAmountPrint = gasAmountPrint + Double.parseDouble(values[2]);
                            co2AmountPrint = co2AmountPrint + Double.parseDouble(values[3]);
                        }

                    }
                    String savedTimeString = dfTime.format(savedTimePrint);
                    String gasAmountString = df.format(gasAmountPrint);
                    String coOutputString = df.format(co2AmountPrint);
                    //setzt dementsprechnend die Werte neu in der Ansicht die für den Nutzer sichtbar ist
                    savedTimeStatText.setText(savedTimeString + " min");
                    co2StatText.setText(coOutputString + " kg");
                    gasAmountStatText.setText(gasAmountString + " l");

                } else if (position == 2) {
                    //macht den String Builder in einen String
                    String message = test;
                    //splitetet den String immer am Hashtag auf
                    String[] rawValuesSplit = message.split("#");
                    //holt sich die Länge des Arrays um iterieren zu können
                    int size = rawValuesSplit.length;
                    //splitet den Stamp auf damit wir davon das Jahr bestimmen können
                    String[] rawDate = stamp.split("\\.");
                    //speichert das Jahr ab
                    String year = rawDate[2];
                    for (int i = 0; i < size; i++) {
                        String rawValues = rawValuesSplit[i];
                        String[] values = rawValues.split(";");
                        //speichert das Datum zum aufsplitten
                        String a = values[0];
                        //splittet das geholte Datum auf damit das Jahr geholt werden kann
                        String[] rawDateValues = a.split("\\.");
                        //wenn das Jahr dem Stampjahrt gleicht war es der selbe Jahr
                        if (rawDateValues[2].equals(year)) {
                            //zählt die Werte auf die passenden drauf
                            savedTimePrint = savedTimePrint + Double.parseDouble(values[1]);
                            gasAmountPrint = gasAmountPrint + Double.parseDouble(values[2]);
                            co2AmountPrint = co2AmountPrint + Double.parseDouble(values[3]);
                        }
                    }
                    String savedTimeString = dfTime.format(savedTimePrint);
                    String gasAmountString = df.format(gasAmountPrint);
                    String coOutputString = df.format(co2AmountPrint);
                    //setzt dementsprechnend die Werte neu in der Ansicht die für den Nutzer sichtbar ist
                    savedTimeStatText.setText(savedTimeString + " min");
                    co2StatText.setText(coOutputString + " kg");
                    gasAmountStatText.setText(gasAmountString + " l");
                }


            }
        });

        button.setValue(1);

        return root;
    }

    private void openDialog() {
        InfoDialog infoDialog = new InfoDialog();
        infoDialog.show(getFragmentManager(), "info dialog");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //button.setValue(1);

        final StatisticsViewModel viewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(StatisticsViewModel.class);

        //savedTimeStat
        LiveData<String> liveSavedTimeStatValue = viewModel.getSavedTimeStat();
        liveSavedTimeStatValue.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                savedTimeStatText.setText(s);
            }
        });

        //CO2Stat
        LiveData<String> liveCo2StatValue = viewModel.getCo2Stat();
        liveCo2StatValue.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                co2StatText.setText(s);
            }
        });

        //Gas Amount
        LiveData<String> liveGasAmountValue = viewModel.getGasAmountStat();
        liveGasAmountValue.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                gasAmountStatText.setText(s);
            }
        });

    }
}