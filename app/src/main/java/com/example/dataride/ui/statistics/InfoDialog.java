package com.example.dataride.ui.statistics;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class InfoDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Information")
                .setMessage("Die gesparte Zeit berechnet sich aus der Differenz Ihrer tatsächlichen Fahrtdauer und der Zeit, die Sie für die gleiche Strecke ohne das Überschreiten der angegebenen Maximalgeschwindigkeit benötigt hätten. Ihr Spritverbrauch errechnet sich aus der zurückgelegten Distanz und dem angegebenen Durchschnittsverbrauch. Da dieser für die gesamte Strecke angenommen wird, kann der ausgerechnete Verbrauch nur als Richtwert verstanden werden. Der resultierende CO2-Ausstoß ergibt sich durch Ihren gesamten Spritverbrauch und einem in DIN EN 16258 definierten Richtwert für die jeweilige Treibstoffart. Somit ist auch dieser Wert lediglich als Richtwert zu verstehen. Alle Daten werden über GPS-Signale ermittelt, sie sind folglich fehleranfällig für unterirdische Strecken und weitere Störungen/Ausfälle.")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        return builder.create();
    }
}
