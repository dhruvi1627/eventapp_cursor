package com.example.eventapp;

import android.app.Application;
import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class EventApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
    }
} 