package com.example.bleradar.domain.usecase

import com.example.bleradar.core.common.TimeProvider
import com.example.bleradar.domain.logic.BleDeviceFactory
import com.example.bleradar.domain.model.BleDevice
import com.example.bleradar.domain.repository.BleScannerRepository
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ObserveNearbyDevicesUseCase(
    private val scanner: BleScannerRepository,
    private val deviceFactory: BleDeviceFactory,
    private val timeProvider: TimeProvider,
) {
    @OptIn(FlowPreview::class)
    operator fun invoke(): Flow<List<BleDevice>> = channelFlow {
        val devices = MutableStateFlow<Map<String, BleDevice>>(emptyMap())

        launch {
            scanner.scan().collect { advertisement ->
                devices.update { current ->
                    current + (advertisement.id to deviceFactory.create(advertisement, current[advertisement.id]))
                }
            }
        }

        launch {
            while (isActive) {
                delay(EVICTION_INTERVAL)
                val threshold = timeProvider.nowMillis() - STALE_AFTER_MILLIS
                devices.update { current -> current.filterValues { it.lastSeenAtMillis >= threshold } }
            }
        }

        devices
            .sample(EMIT_INTERVAL)
            .map { it.values.sortedByDescending(BleDevice::smoothedRssi) }
            .collect { send(it) }
    }

    private companion object {
        val EMIT_INTERVAL = 200.milliseconds
        val EVICTION_INTERVAL = 1.seconds
        const val STALE_AFTER_MILLIS = 10_000L
    }
}