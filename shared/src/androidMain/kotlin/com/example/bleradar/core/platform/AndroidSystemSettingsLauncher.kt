package com.example.bleradar.core.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

class AndroidSystemSettingsLauncher(private val context: Context) : SystemSettingsLauncher {

    override fun openBluetoothSettings() = launch(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))

    override fun openAppSettings() = launch(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null),
        )
    )

    private fun launch(intent: Intent) {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}