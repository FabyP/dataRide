package com.example.dataride.ui.car;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CarViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private final MutableLiveData<String> speed = new MutableLiveData<>();
    private final MutableLiveData<String> savedTime = new MutableLiveData<>();
    private final MutableLiveData<String> passedTime = new MutableLiveData<>();

    //Uhr
    public CarViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Uhr");
    }
    public LiveData<String> getText() {
        return mText;
    }

    //Current Speed
    public void setSpeed(String speed){
        this.speed.setValue(speed);
    }
    public LiveData<String> getSpeed(){
        return this.speed;
    }

    //Saved Time
    public void setSavedTime(String savedTime){
        this.savedTime.setValue(savedTime);
    }
    public LiveData<String> getSavedTime(){
        return this.savedTime;
    }

    //Passed Time
    public void setPassedTime(String passedTime){
        this.passedTime.setValue(passedTime);
    }
    public LiveData<String> getPassedTime(){
        return this.passedTime;
    }
}