package com.example.dataride.ui.car;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CarViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private final MutableLiveData<String> speed = new MutableLiveData<>();

    public CarViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Uhr");
    }

    public LiveData<String> getText() {
        return mText;
    }

    public void setSpeed(String speed){
        this.speed.setValue(speed);
    }

    public LiveData<String> getSpeed(){
        return this.speed;
    }
}