package com.example.bleradar.presentation.feature.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bleradar.core.platform.PermissionResult
import com.example.bleradar.core.platform.SystemSettingsLauncher
import com.example.bleradar.domain.model.BleDevice
import com.example.bleradar.domain.model.BleError
import com.example.bleradar.domain.model.BleException
import com.example.bleradar.domain.model.BluetoothState
import com.example.bleradar.domain.repository.BleScannerRepository
import com.example.bleradar.domain.repository.DeviceHistoryRepository
import com.example.bleradar.domain.usecase.DeviceFilter
import com.example.bleradar.domain.usecase.FilterDevicesUseCase
import com.example.bleradar.domain.usecase.ObserveNearbyDevicesUseCase
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@OptIn(FlowPreview::class)
class ScannerViewModel(
    private val observeNearbyDevices: ObserveNearbyDevicesUseCase,
    private val filterDevices: FilterDevicesUseCase,
    scanner: BleScannerRepository,
    private val historyRepository: DeviceHistoryRepository,
    private val settingsLauncher: SystemSettingsLauncher,
) : ViewModel() {

    private val detectedDevices = MutableStateFlow<List<BleDevice>>(emptyList())
    private val filter = MutableStateFlow(DeviceFilter())
    private val isScanning = MutableStateFlow(false)
    private val bluetoothEnabled = MutableStateFlow(true)
    private val issue = MutableStateFlow<ScannerIssue?>(null)

    private val effectChannel = Channel<ScannerEffect>(Channel.BUFFERED)
    val effects: Flow<ScannerEffect> = effectChannel.receiveAsFlow()

    val uiState: StateFlow<ScannerUiState> = combine(
        detectedDevices,
        filter,
        isScanning,
        bluetoothEnabled,
        issue,
    ) { devices, currentFilter, scanning, bluetooth, currentIssue ->
        val activeDevices = if (bluetooth) devices else emptyList()
        ScannerUiState(
            isScanning = scanning,
            devices = filterDevices(activeDevices, currentFilter),
            totalDetected = activeDevices.size,
            query = currentFilter.query,
            minRssi = currentFilter.minRssi,
            issue = if (bluetooth) currentIssue else ScannerIssue.BluetoothOff,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScannerUiState())

    private var permission: PermissionResult? = null
    private var scanRequested = false
    private var scanJob: Job? = null

    init {
        scanner.observeBluetoothState()
            .onEach(::onBluetoothStateChanged)
            .launchIn(viewModelScope)

        detectedDevices
            .filter { it.isNotEmpty() }
            .sample(HISTORY_INTERVAL)
            .onEach(::recordHistory)
            .launchIn(viewModelScope)
    }

    fun onEvent(event: ScannerEvent) {
        when (event) {
            ScannerEvent.StartScan -> startScan()
            ScannerEvent.StopScan -> stopScan()
            ScannerEvent.Foregrounded -> resumeIfRequested()
            ScannerEvent.Backgrounded -> cancelScan()
            ScannerEvent.OpenBluetoothSettings -> settingsLauncher.openBluetoothSettings()
            ScannerEvent.OpenAppSettings -> settingsLauncher.openAppSettings()
            is ScannerEvent.QueryChanged -> filter.update { it.copy(query = event.query) }
            is ScannerEvent.MinRssiChanged -> filter.update { it.copy(minRssi = event.minRssi) }
            is ScannerEvent.PermissionChecked -> onPermissionChecked(event.isGranted)
            is ScannerEvent.PermissionResolved -> onPermissionResolved(event.result)
        }
    }

    private fun startScan() {
        scanRequested = true
        if (permission == PermissionResult.GRANTED) {
            resumeIfRequested()
        } else {
            effectChannel.trySend(ScannerEffect.RequestPermission)
        }
    }

    private fun stopScan() {
        scanRequested = false
        cancelScan()
        detectedDevices.value = emptyList()
    }

    private fun resumeIfRequested() {
        val canScan = scanRequested && permission == PermissionResult.GRANTED && bluetoothEnabled.value
        if (canScan && scanJob?.isActive != true) beginScan()
    }

    private fun beginScan() {
        issue.value = null
        isScanning.value = true
        scanJob = observeNearbyDevices()
            .onEach { detectedDevices.value = it }
            .catch { onScanFailure(it) }
            .launchIn(viewModelScope)
    }

    private fun cancelScan() {
        scanJob?.cancel()
        scanJob = null
        isScanning.value = false
    }

    private fun onScanFailure(error: Throwable) {
        scanJob = null
        isScanning.value = false
        detectedDevices.value = emptyList()
        issue.value = when (val bleError = (error as? BleException)?.error) {
            BleError.BluetoothDisabled -> null
            is BleError.ScanFailed -> ScannerIssue.ScanFailed(bleError.message)
            null -> ScannerIssue.ScanFailed(error.message)
        }
    }

    private fun onBluetoothStateChanged(state: BluetoothState) {
        val enabled = state == BluetoothState.ENABLED
        bluetoothEnabled.value = enabled
        if (enabled) {
            resumeIfRequested()
        } else {
            cancelScan()
            detectedDevices.value = emptyList()
        }
    }

    private fun onPermissionChecked(isGranted: Boolean) {
        when {
            isGranted -> {
                permission = PermissionResult.GRANTED
                issue.value = null
                resumeIfRequested()
            }
            permission == PermissionResult.GRANTED -> {
                permission = null
                cancelScan()
            }
        }
    }

    private fun onPermissionResolved(result: PermissionResult) {
        permission = result
        when (result) {
            PermissionResult.GRANTED -> {
                issue.value = null
                resumeIfRequested()
            }
            PermissionResult.DENIED -> {
                scanRequested = false
                issue.value = ScannerIssue.PermissionDenied
            }
            PermissionResult.PERMANENTLY_DENIED -> {
                scanRequested = false
                issue.value = ScannerIssue.PermissionBlocked
            }
        }
    }

    private suspend fun recordHistory(devices: List<BleDevice>) {
        try {
            historyRepository.record(devices)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
        }
    }

    private companion object {
        val HISTORY_INTERVAL = 5.seconds
    }
}