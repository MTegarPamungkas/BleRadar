package com.example.bleradar.domain.usecase

import com.example.bleradar.domain.logic.BleDeviceFactory
import com.example.bleradar.domain.model.BleDevice
import com.example.bleradar.domain.model.BleError
import com.example.bleradar.domain.model.BleException
import com.example.bleradar.domain.model.SignalZone
import com.example.bleradar.domain.model.TrackingState
import com.example.bleradar.domain.repository.BleScannerRepository
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.transformLatest

private data class Tracked(
    val device: BleDevice? = null,
    val belowSinceMillis: Long? = null,
)

class TrackDeviceUseCase(
    private val scanner: BleScannerRepository,
    private val deviceFactory: BleDeviceFactory,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(deviceId: String): Flow<TrackingState> =
        scanner.scan()
            .filter { it.id == deviceId }
            .scan(Tracked()) { acc, advertisement ->
                val device = deviceFactory.create(advertisement, acc.device)
                if (device.zone != SignalZone.LOST) return@scan Tracked(device, null)

                val since = acc.belowSinceMillis ?: advertisement.receivedAtMillis
                val confirmed = advertisement.receivedAtMillis - since >= LOST_RSSI_GRACE_MS
                Tracked(
                    device = if (confirmed) device else device.copy(zone = SignalZone.VERY_WEAK),
                    belowSinceMillis = since,
                )
            }
            .mapNotNull { it.device }
            .transformLatest { device ->
                emit(
                    if (device.zone == SignalZone.LOST) TrackingState.Lost(device)
                    else TrackingState.Active(device)
                )
                delay(LOST_TIMEOUT)  
                emit(TrackingState.Lost(device))
            }
            .catch { emit(TrackingState.Failed(it.toBleError())) }
            .onStart { emit(TrackingState.Searching) }

    private fun Throwable.toBleError(): BleError =
        (this as? BleException)?.error ?: BleError.ScanFailed(message)

    private companion object {
        val LOST_TIMEOUT = 10.seconds
        const val LOST_RSSI_GRACE_MS = 3_000L
    }
}