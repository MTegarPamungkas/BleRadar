package com.example.bleradar.core.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.bleradar.core.platform.AndroidSystemSettingsLauncher
import com.example.bleradar.core.platform.SystemSettingsLauncher
import com.example.bleradar.data.local.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        val context = androidContext()
        Room.databaseBuilder<AppDatabase>(
            context = context,
            name = context.getDatabasePath("ble_radar.db").absolutePath,
        )
    }
    single<SystemSettingsLauncher> { AndroidSystemSettingsLauncher(androidContext()) }
}