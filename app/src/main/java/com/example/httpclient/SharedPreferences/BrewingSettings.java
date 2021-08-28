package com.example.httpclient.SharedPreferences;

import android.content.SharedPreferences;

public class BrewingSettings {

    SharedPreferences sharedPreferences;

    //Pref Name
    public static final String PREFERENCE_NAME = "BREWING_SETTINGS";

    //Keys
    private final String TEMPERATURE_TOLERANCE = "TEMPERATURE_TOLERANCE";

    public BrewingSettings(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public int getTempTolerance(){
        return sharedPreferences.getInt(TEMPERATURE_TOLERANCE, 1);
    }


}
