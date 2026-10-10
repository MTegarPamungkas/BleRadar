package com.example.bleradar.domain.model

import kotlin.math.pow
import kotlin.math.sqrt

enum class SignalStability {
    UNKNOWN, STABLE, MODERATE, UNSTABLE;

    companion object {
        private const val MIN_SAMPLES = 10

        fun from(samples: List<Int>): SignalStability {
            if (samples.size < MIN_SAMPLES) return UNKNOWN
            val mean = samples.average()
            val deviation = sqrt(samples.sumOf { (it - mean).pow(2) } / samples.size)
            return when {
                deviation < 3.0 -> STABLE
                deviation < 6.0 -> MODERATE
                else -> UNSTABLE
            }
        }
    }
}