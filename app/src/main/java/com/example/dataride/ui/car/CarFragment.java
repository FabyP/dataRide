package com.example.dataride.ui.car;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.dataride.MainActivity;
import com.example.dataride.OnChangeListener;
import com.example.dataride.R;

import java.util.Observable;


public class CarFragment extends Fragment {

    private CarViewModel carViewModel;
    private TextView speedText;
    OnChangeListener SpeedListener;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        carViewModel =
                ViewModelProviders.of(this).get(CarViewModel.class);
        View root = inflater.inflate(R.layout.fragment_car, container, false);
        final TextView textView = root.findViewById(R.id.text_time);
        carViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        MainActivity activity = (MainActivity) getActivity();
        speedText = (TextView)root.findViewById(R.id.currentSpeed);
        speedText.setText(activity.speedFromMainActivity());
        return root;
    }
}