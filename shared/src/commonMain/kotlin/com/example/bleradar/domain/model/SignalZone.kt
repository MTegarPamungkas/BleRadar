package com.example.bleradar.domain.model

enum class SignalZone {
    VERY_STRONG, STRONG, GOOD, WEAK, VERY_WEAK, LOST;

    companion object {
        fun fromRssi(rssi: Int): SignalZone = when {
            rssi >= -30 -> VERY_STRONG
            rssi >= -50 -> STRONG
            rssi >= -70 -> GOOD
            rssi >= -80 -> WEAK
            rssi >= -90 -> VERY_WEAK
            else -> LOST
        }
    }
}