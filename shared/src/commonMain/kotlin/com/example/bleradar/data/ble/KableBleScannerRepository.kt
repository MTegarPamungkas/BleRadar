package com.example.bleradar.data.ble

import com.example.bleradar.core.common.TimeProvider
import com.example.bleradar.domain.model.BleAdvertisement
import com.example.bleradar.domain.model.BleError
import com.example.bleradar.domain.model.BleException
import com.example.bleradar.domain.model.BluetoothState
import com.example.bleradar.domain.repository.BleScannerRepository
import com.juul.kable.Advertisement
import com.juul.kable.Bluetooth
import com.juul.kable.UnmetRequirementException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.transform

@Suppress("DEPRECATION")
class KableBleScannerRepository(
    appScope: CoroutineScope,
    private val timeProvider: TimeProvider,
) : BleScannerRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val events: SharedFlow<ScanEvent> = observeBluetoothState()
        .flatMapLatest { state ->
            if (state == BluetoothState.ENABLED) {
                createBleScanner()
                    .advertisements
                    .buffer(Channel.BUFFERED)
                    .map<Advertisement, ScanEvent> { ScanEvent.Found(it.toDomain(timeProvider.nowMillis())) }
                    .catch { emit(ScanEvent.Failed(it.toBleError())) }
            } else {
                flowOf(ScanEvent.Failed(BleError.BluetoothDisabled))
            }
        }
        .shareIn(appScope, SharingStarted.WhileSubscribed())

    override fun scan(): Flow<BleAdvertisement> = events.transform { event ->
        when (event) {
            is ScanEvent.Found -> emit(event.advertisement)
            is ScanEvent.Failed -> throw BleException(event.error)
        }
    }

    override fun observeBluetoothState(): Flow<BluetoothState> =
        Bluetooth.availability
            .map { availability ->
                when (availability) {
                    is Bluetooth.Availability.Available -> BluetoothState.ENABLED
                    is Bluetooth.Availability.Unavailable -> BluetoothState.DISABLED
                }
            }
            .distinctUntilChanged()
}

private sealed interface ScanEvent {
    data class Found(val advertisement: BleAdvertisement) : ScanEvent
    data class Failed(val error: BleError) : ScanEvent
}

private fun Advertisement.toDomain(now: Long) = BleAdvertisement(
    id = identifier,
    name = name ?: peripheralName,
    rssi = rssi,
    receivedAtMillis = now,
)

private fun Throwable.toBleError(): BleError = when (this) {
    is UnmetRequirementException -> BleError.BluetoothDisabled
    else -> BleError.ScanFailed(message)
}
