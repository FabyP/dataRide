package com.example.dataride.ui.car;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CarViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public CarViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Uhr");
    }

    public LiveData<String> getText() {
        return mText;
    }
}