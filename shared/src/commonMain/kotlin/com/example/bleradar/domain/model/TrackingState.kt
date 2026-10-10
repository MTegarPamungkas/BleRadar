package com.example.bleradar.domain.model

sealed interface TrackingState {
    data object Searching : TrackingState
    data class Active(val device: BleDevice) : TrackingState
    data class Lost(val lastKnown: BleDevice) : TrackingState
    data class Failed(val error: BleError) : TrackingState
}

val TrackingState.currentDevice: BleDevice?
    get() = when (this) {
        TrackingState.Searching, is TrackingState.Failed -> null
        is TrackingState.Active -> device
        is TrackingState.Lost -> lastKnown
    }