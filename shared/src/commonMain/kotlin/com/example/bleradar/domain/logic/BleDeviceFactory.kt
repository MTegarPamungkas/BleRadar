package com.example.bleradar.domain.logic

import com.example.bleradar.domain.model.BleAdvertisement
import com.example.bleradar.domain.model.BleDevice
import com.example.bleradar.domain.model.SignalZone

class BleDeviceFactory(
    private val estimator: DistanceEstimator,
    private val smoother: RssiSmoother,
) {
    fun create(advertisement: BleAdvertisement, previous: BleDevice?): BleDevice {
        val smoothed = smoother.smooth(previous?.smoothedRssi, advertisement.rssi)
        return BleDevice(
            id = advertisement.id,
            name = advertisement.name ?: previous?.name,
            rssi = advertisement.rssi,
            smoothedRssi = smoothed,
            distanceMeters = estimator.estimate(smoothed),
            zone = SignalZone.fromRssi(smoothed),
            lastSeenAtMillis = advertisement.receivedAtMillis,
        )
    }
}