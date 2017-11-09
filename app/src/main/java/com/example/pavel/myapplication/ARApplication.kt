package com.example.pavel.myapplication

import android.app.Application
import com.example.pavel.myapplication.di.appModules
import org.koin.android.ext.android.startAndroidContext

class ARApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startAndroidContext(this, appModules())
    }
}