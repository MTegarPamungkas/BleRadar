package com.example.bleradar.presentation.feature.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bleradar.core.designsystem.color
import com.example.bleradar.core.designsystem.label
import com.example.bleradar.core.platform.rememberBlePermissionRequester
import com.example.bleradar.domain.model.BleDevice
import com.example.bleradar.presentation.common.distanceLabel
import org.koin.compose.viewmodel.koinViewModel

private val RssiThresholds = listOf(null, -60, -70, -80, -90)

@Composable
fun ScannerRoute(
    onDeviceClick: (String) -> Unit,
    onHistoryClick: () -> Unit,
) {
    val viewModel = koinViewModel<ScannerViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionRequester = rememberBlePermissionRequester { result ->
        viewModel.onEvent(ScannerEvent.PermissionResolved(result))
    }

    LaunchedEffect(viewModel, permissionRequester) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ScannerEffect.RequestPermission -> permissionRequester.request()
            }
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        viewModel.onEvent(ScannerEvent.PermissionChecked(permissionRequester.isGranted))
        viewModel.onEvent(ScannerEvent.Foregrounded)
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.onEvent(ScannerEvent.Backgrounded)
    }

    ScannerScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onDeviceClick = onDeviceClick,
        onHistoryClick = onHistoryClick,
    )
}

@Composable
fun ScannerScreen(
    state: ScannerUiState,
    onEvent: (ScannerEvent) -> Unit,
    onDeviceClick: (String) -> Unit,
    onHistoryClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BLE Radar") },
                actions = { TextButton(onClick = onHistoryClick) { Text("Riwayat") } },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    onEvent(if (state.isScanning) ScannerEvent.StopScan else ScannerEvent.StartScan)
                },
            ) {
                Text(if (state.isScanning) "Stop" else "Start")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { onEvent(ScannerEvent.QueryChanged(it)) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                placeholder = { Text("Cari nama atau MAC address") },
                singleLine = true,
            )
            RssiFilterRow(selected = state.minRssi) { onEvent(ScannerEvent.MinRssiChanged(it)) }
            state.issue?.let { IssueBanner(it, onEvent) }
            Text(
                text = "${state.devices.size} dari ${state.totalDetected} perangkat",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.labelMedium,
            )
            if (state.devices.isEmpty()) {
                EmptyState(isScanning = state.isScanning)
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 88.dp)) {
                    items(state.devices, key = { it.id }) { device ->
                        DeviceItem(
                            device = device,
                            onClick = { onDeviceClick(device.id) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RssiFilterRow(selected: Int?, onSelected: (Int?) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(RssiThresholds) { threshold ->
            FilterChip(
                selected = selected == threshold,
                onClick = { onSelected(threshold) },
                label = { Text(threshold?.let { "≥ $it dBm" } ?: "Semua") },
            )
        }
    }
}

@Composable
private fun IssueBanner(issue: ScannerIssue, onEvent: (ScannerEvent) -> Unit) {
    val (message, actionLabel, action) = when (issue) {
        ScannerIssue.BluetoothOff -> Triple(
            "Bluetooth nonaktif. Aktifkan Bluetooth (dan Layanan Lokasi di Android 11 ke bawah) untuk memindai.",
            "Buka Pengaturan",
            ScannerEvent.OpenBluetoothSettings,
        )
        ScannerIssue.PermissionDenied -> Triple(
            "Izin pemindaian Bluetooth diperlukan untuk menemukan perangkat.",
            "Beri Izin",
            ScannerEvent.StartScan,
        )
        ScannerIssue.PermissionBlocked -> Triple(
            "Izin ditolak permanen. Aktifkan lewat pengaturan aplikasi.",
            "Buka Pengaturan",
            ScannerEvent.OpenAppSettings,
        )
        is ScannerIssue.ScanFailed -> Triple(
            "Pemindaian gagal${issue.message?.let { ": $it" }.orEmpty()}",
            "Coba Lagi",
            ScannerEvent.StartScan,
        )
    }
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(message, color = MaterialTheme.colorScheme.onErrorContainer)
            TextButton(onClick = { onEvent(action) }) { Text(actionLabel) }
        }
    }
}

@Composable
private fun EmptyState(isScanning: Boolean) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(if (isScanning) "Mencari perangkat di sekitar…" else "Tekan Start untuk mulai memindai")
    }
}

@Composable
private fun DeviceItem(device: BleDevice, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        leadingContent = {
            Box(Modifier.size(12.dp).background(device.zone.color(), CircleShape))
        },
        headlineContent = {
            Text(device.name ?: "Perangkat tanpa nama", maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = { Text("${device.id} • ${device.zone.label()}") },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(device.distanceLabel(), style = MaterialTheme.typography.titleSmall)
                Text("${device.rssi} dBm", style = MaterialTheme.typography.bodySmall)
            }
        },
    )
}