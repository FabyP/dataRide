package com.example.dataride.ui.statistics;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StatisticsViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private final MutableLiveData<String> savedTimeStat = new MutableLiveData<>();
    private final MutableLiveData<String> co2Stat = new MutableLiveData<>();
    private final MutableLiveData<String> gasAmountStat = new MutableLiveData<>();

    public StatisticsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Statistik");
    }
    public LiveData<String> getText() {
        return mText;
    }

    //Saved Time Statistik
    public void setSavedTimeStat(String savedTimeStat){
        this.savedTimeStat.setValue(savedTimeStat);
    }
    public LiveData<String> getSavedTimeStat(){
        return this.savedTimeStat;
    }

    //co2 Ausstoß
    public void setCo2Stat(String co2Stat){
        this.co2Stat.setValue(co2Stat);
    }
    public LiveData<String> getCo2Stat(){
        return this.co2Stat;
    }

    //Spritverbrauch
    public void setGasAmountStat(String gasAmountStat){
        this.gasAmountStat.setValue(gasAmountStat);
    }
    public MutableLiveData<String> getGasAmountStat() {
        return this.gasAmountStat;
    }
}