package com.pavel.augmented

import android.app.Application
import com.pavel.augmented.di.appModules
import org.koin.android.ext.android.startAndroidContext

class ARApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startAndroidContext(this, appModules())
    }
}