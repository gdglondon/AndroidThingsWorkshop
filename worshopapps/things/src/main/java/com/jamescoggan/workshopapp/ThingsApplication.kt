package com.jamescoggan.workshopapp

import android.app.Application
import com.google.firebase.FirebaseApp
import timber.log.Timber

class ThingsApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        Timber.plant(Timber.DebugTree())
    }
}
