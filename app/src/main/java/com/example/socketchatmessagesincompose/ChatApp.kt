package com.example.socketchatmessagesincompose

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class ChatApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for better logging
        Timber.plant(Timber.DebugTree())
    }
}