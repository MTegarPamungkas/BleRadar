package com.example.bleradar.presentation.feature.scanner

import com.example.bleradar.core.platform.PermissionResult
import com.example.bleradar.domain.model.BleDevice

data class ScannerUiState(
    val isScanning: Boolean = false,
    val devices: List<BleDevice> = emptyList(),
    val totalDetected: Int = 0,
    val query: String = "",
    val minRssi: Int? = null,
    val issue: ScannerIssue? = null,
)

sealed interface ScannerIssue {
    data object BluetoothOff : ScannerIssue
    data object PermissionDenied : ScannerIssue
    data object PermissionBlocked : ScannerIssue
    data class ScanFailed(val message: String?) : ScannerIssue
}

sealed interface ScannerEvent {
    data object StartScan : ScannerEvent
    data object StopScan : ScannerEvent
    data object Foregrounded : ScannerEvent
    data object Backgrounded : ScannerEvent
    data object OpenBluetoothSettings : ScannerEvent
    data object OpenAppSettings : ScannerEvent
    data class QueryChanged(val query: String) : ScannerEvent
    data class MinRssiChanged(val minRssi: Int?) : ScannerEvent
    data class PermissionChecked(val isGranted: Boolean) : ScannerEvent
    data class PermissionResolved(val result: PermissionResult) : ScannerEvent
}

sealed interface ScannerEffect {
    data object RequestPermission : ScannerEffect
}