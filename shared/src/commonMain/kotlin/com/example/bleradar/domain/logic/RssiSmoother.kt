package com.example.bleradar.domain.logic

import kotlin.math.abs
import kotlin.math.roundToInt

class RssiSmoother(
    private val alpha: Double = 0.3,
    private val fastAlpha: Double = 0.7,
    private val jumpThresholdDb: Int = 8,
) {
    fun smooth(previous: Int?, current: Int): Int {
        if (previous == null) return current
        val a = if (abs(current - previous) >= jumpThresholdDb) fastAlpha else alpha
        return (a * current + (1 - a) * previous).roundToInt()
    }
}