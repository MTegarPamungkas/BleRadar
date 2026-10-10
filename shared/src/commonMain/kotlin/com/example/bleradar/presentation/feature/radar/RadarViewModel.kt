package com.example.bleradar.presentation.feature.radar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bleradar.domain.model.BleError
import com.example.bleradar.domain.model.BluetoothState
import com.example.bleradar.domain.model.SignalStability
import com.example.bleradar.domain.model.TrackingState
import com.example.bleradar.domain.repository.BleScannerRepository
import com.example.bleradar.domain.usecase.TrackDeviceUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn

data class RadarUiState(
    val tracking: TrackingState = TrackingState.Searching,
    val samples: List<Int> = emptyList(),
    val bluetoothEnabled: Boolean = true,
) {
    val stability: SignalStability get() = SignalStability.from(samples)
}

class RadarViewModel(
    deviceId: String,
    trackDevice: TrackDeviceUseCase,
    scanner: BleScannerRepository,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<RadarUiState> = scanner.observeBluetoothState()
        .onStart { emit(BluetoothState.ENABLED) }
        .flatMapLatest { bluetooth ->
            if (bluetooth == BluetoothState.ENABLED) {
                trackDevice(deviceId)
                    .scan(RadarUiState(bluetoothEnabled = true)) { state, tracking -> state.reduce(tracking) }
            } else {
                flowOf(
                    RadarUiState(
                        tracking = TrackingState.Failed(BleError.BluetoothDisabled),
                        bluetoothEnabled = false,
                    )
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RadarUiState())

    private fun RadarUiState.reduce(tracking: TrackingState): RadarUiState {
        val device = (tracking as? TrackingState.Active)?.device
        return copy(
            tracking = tracking,
            samples = if (device == null) samples else (samples + device.rssi).takeLast(MAX_SAMPLES),
        )
    }

    private companion object {
        const val MAX_SAMPLES = 40
    }
}
