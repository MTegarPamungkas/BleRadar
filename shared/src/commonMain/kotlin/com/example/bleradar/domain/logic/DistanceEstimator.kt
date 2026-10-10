package com.example.bleradar.domain.logic

import kotlin.math.pow

class DistanceEstimator {

    private val anchors = listOf(
        -90 to 40.0,
        -80 to 20.0,
        -70 to 10.0,
        -50 to 3.0,
        -30 to 1.0,
        -10 to 0.1,
    )

    fun estimate(rssi: Int): Double = estimate(rssi.toDouble())

    fun estimate(rssi: Double): Double {
        val value = rssi.coerceIn(MIN_RSSI_DBM, MAX_RSSI_DBM)
        val upper = anchors.indexOfFirst { it.first >= value }.coerceAtLeast(1)
        val (rssiLow, distLow) = anchors[upper - 1]
        val (rssiHigh, distHigh) = anchors[upper]

        val fraction = (value - rssiLow) / (rssiHigh - rssiLow)
        return distLow * (distHigh / distLow).pow(fraction)
    }

    private companion object {
        const val MIN_RSSI_DBM = -90.0
        const val MAX_RSSI_DBM = -10.0
    }
}