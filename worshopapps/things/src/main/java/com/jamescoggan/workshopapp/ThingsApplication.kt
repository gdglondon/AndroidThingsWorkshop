package com.jamescoggan.workshopapp

import android.app.Application
import timber.log.Timber

class ThingsApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
    }
}
