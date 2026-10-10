package com.example.bleradar

import androidx.compose.runtime.Composable
import com.example.bleradar.core.designsystem.BleRadarTheme
import com.example.bleradar.core.navigation.AppNavHost

@Composable
fun App() {
    BleRadarTheme {
        AppNavHost()
    }
}