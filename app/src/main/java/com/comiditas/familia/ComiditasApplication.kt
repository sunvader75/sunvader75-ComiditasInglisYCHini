package com.comiditas.familia

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ComiditasApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("Comiditas", "Application onCreate")
    }
}
