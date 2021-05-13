package com.example.httpclient.SharedPreferences;

import android.content.SharedPreferences;

public class WiFiSettings {

    SharedPreferences sharedPreferences;

    //Pref Name
    public static final String PREFERENCE_NAME = "WIFI_PREF";

    //Keys
    private final String IP = "IP";

    public WiFiSettings(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public String getIP(){
        return sharedPreferences.getString(IP, "0.0.0.0");
    }


}
