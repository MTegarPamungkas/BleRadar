package com.example.bleradar.core.designsystem

import androidx.compose.ui.graphics.Color
import com.example.bleradar.domain.model.SignalStability
import com.example.bleradar.domain.model.SignalZone

fun SignalZone.label(): String = when (this) {
    SignalZone.VERY_STRONG -> "Sangat Kuat"
    SignalZone.STRONG -> "Kuat"
    SignalZone.GOOD -> "Cukup / Baik"
    SignalZone.WEAK -> "Lemah"
    SignalZone.VERY_WEAK -> "Sangat Lemah"
    SignalZone.LOST -> "Sinyal Hilang"
}

fun SignalZone.rangeLabel(): String = when (this) {
    SignalZone.VERY_STRONG -> "< 1 m"
    SignalZone.STRONG -> "1 – 3 m"
    SignalZone.GOOD -> "3 – 10 m"
    SignalZone.WEAK -> "10 – 20 m"
    SignalZone.VERY_WEAK -> "> 20 m"
    SignalZone.LOST -> "Di luar jangkauan"
}

fun SignalZone.color(): Color = when (this) {
    SignalZone.VERY_STRONG -> Color(0xFF00C853)
    SignalZone.STRONG -> Color(0xFF64DD17)
    SignalZone.GOOD -> Color(0xFFFFD600)
    SignalZone.WEAK -> Color(0xFFFF9100)
    SignalZone.VERY_WEAK -> Color(0xFFFF3D00)
    SignalZone.LOST -> Color(0xFF9E9E9E)
}

fun SignalStability.label(): String = when (this) {
    SignalStability.UNKNOWN -> "—"
    SignalStability.STABLE -> "Stabil"
    SignalStability.MODERATE -> "Sedang"
    SignalStability.UNSTABLE -> "Tidak stabil"
}