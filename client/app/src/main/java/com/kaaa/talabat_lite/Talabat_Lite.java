package com.kaaa.talabat_lite;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

// To force the app to run in light mode for all activities.
public class Talabat_Lite extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}