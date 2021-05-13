package com.example.httpclient.Utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesEditor {

    SharedPreferences sharedPreferences;

    public static final String STATUS = "STATUS";
    public static final String CURRENT_STEP = "CURRENT_STEP";
    public static final String TIME_REMAIN = "TIME_REMAIN";

    public SharedPreferencesEditor(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public void putInt(String key, int value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public void putString(String key, String value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public int getInt(String key, int defaultValue){
        return sharedPreferences.getInt(key, defaultValue);
    }

    public String getString(String key, String defaultValue){
        return sharedPreferences.getString(key, defaultValue);
    }


}
