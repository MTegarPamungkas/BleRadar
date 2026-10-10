package com.example.bleradar.domain.model

data class BleAdvertisement(
    val id: String,
    val name: String?,
    val rssi: Int,
    val receivedAtMillis: Long,
)

data class BleDevice(
    val id: String,
    val name: String?,
    val rssi: Int,
    val smoothedRssi: Int,
    val distanceMeters: Double,
    val zone: SignalZone,
    val lastSeenAtMillis: Long,
)

data class DeviceHistory(
    val id: String,
    val name: String?,
    val lastRssi: Int,
    val firstSeenAtMillis: Long,
    val lastSeenAtMillis: Long,
)

enum class BluetoothState { ENABLED, DISABLED }

sealed interface BleError {
    data object BluetoothDisabled : BleError
    data class ScanFailed(val message: String?) : BleError
}

class BleException(val error: BleError) : Exception()