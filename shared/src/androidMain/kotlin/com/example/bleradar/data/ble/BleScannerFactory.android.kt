package com.example.bleradar.data.ble

import android.bluetooth.le.ScanSettings
import com.juul.kable.Advertisement
import com.juul.kable.ObsoleteKableApi
import com.juul.kable.Scanner

actual fun createBleScanner(): Scanner<Advertisement> = Scanner {
    @OptIn(ObsoleteKableApi::class)
    scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setReportDelay(0)
        .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
        .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        .build()
}
