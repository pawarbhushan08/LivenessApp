package com.bhushan.android.livenessapp

import android.app.Application
import com.bhushan.android.livenessapp.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class LivenessApplication: Application()  {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@LivenessApplication)
            modules(appModules)
        }

    }
}