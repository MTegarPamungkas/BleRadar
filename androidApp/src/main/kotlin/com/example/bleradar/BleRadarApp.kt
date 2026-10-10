package com.example.bleradar

import android.app.Application
import com.example.bleradar.core.di.initKoin
import org.koin.android.ext.koin.androidContext

class BleRadarApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin { androidContext(this@BleRadarApp) }
    }
}