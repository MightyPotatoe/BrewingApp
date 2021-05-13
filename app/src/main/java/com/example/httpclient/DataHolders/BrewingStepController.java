package com.example.httpclient.DataHolders;

import android.content.SharedPreferences;

public class BrewingStepController {

    private int stepNumber;
    private int currentStepTemperature;
    private int currentStepTime;

    private SharedPreferences sharedPreferences;

    public BrewingStepController(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        stepNumber = sharedPreferences.getInt("CURRENT_STEP", 0);
    }


    public void getTemperature(){

    }


}
