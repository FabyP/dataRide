package com.example.dataride.ui.statistics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.dataride.R;

import java.util.Objects;

public class StatisticsFragment extends Fragment {

    private TextView savedTimeStatText;
    private TextView co2StatText;
    private TextView gasAmountStatText;
    private ImageButton infoButton;

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

        return root;
    }

    private void openDialog() {
        InfoDialog infoDialog = new InfoDialog();
        infoDialog.show(getFragmentManager(), "info dialog");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final StatisticsViewModel viewModel  = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(StatisticsViewModel.class);

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