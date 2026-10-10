package com.example.bleradar.presentation.feature.radar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bleradar.core.designsystem.color
import com.example.bleradar.core.designsystem.label
import com.example.bleradar.core.designsystem.rangeLabel
import com.example.bleradar.domain.model.SignalZone
import com.example.bleradar.domain.model.TrackingState
import com.example.bleradar.domain.model.currentDevice
import com.example.bleradar.presentation.common.distanceLabel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun RadarRoute(deviceId: String, onBack: () -> Unit) {
    val viewModel = koinViewModel<RadarViewModel>(parameters = { parametersOf(deviceId) })
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RadarScreen(deviceId = deviceId, state = state, onBack = onBack)
}

@Composable
fun RadarScreen(deviceId: String, state: RadarUiState, onBack: () -> Unit) {
    val device = state.tracking.currentDevice
    val active = (state.tracking as? TrackingState.Active)?.device
    val zone = active?.zone ?: SignalZone.LOST

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(device?.name ?: "Pelacakan") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Kembali") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (!state.bluetoothEnabled) {
                Text("Bluetooth nonaktif", color = MaterialTheme.colorScheme.error)
            }
            RadarView(
                deviceId = deviceId,
                distanceMeters = active?.distanceMeters,
                zone = zone,
                modifier = Modifier.fillMaxWidth(0.9f),
            )
            StatusHeader(state.tracking)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile("RSSI", device?.let { "${it.rssi} dBm" } ?: "—", Modifier.weight(1f))
                MetricTile("Rata-rata", device?.let { "${it.smoothedRssi} dBm" } ?: "—", Modifier.weight(1f))
                MetricTile("Stabilitas", state.stability.label(), Modifier.weight(1f))
            }
            RssiSparkline(
                samples = state.samples,
                color = zone.color(),
                modifier = Modifier.fillMaxWidth().height(72.dp),
            )
        }
    }
}

@Composable
private fun StatusHeader(tracking: TrackingState) {
    when (tracking) {
        TrackingState.Searching -> Text("Mencari perangkat…", style = MaterialTheme.typography.titleMedium)
        is TrackingState.Active -> {
            Text(tracking.device.distanceLabel(), style = MaterialTheme.typography.displaySmall)
            Text(
                text = "${tracking.device.zone.label()} • ${tracking.device.zone.rangeLabel()}",
                color = tracking.device.zone.color(),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        is TrackingState.Lost -> Text(
            text = "Sinyal hilang — di luar jangkauan",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleMedium,
        )
        is TrackingState.Failed -> Text(
            text = "Pelacakan gagal",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun MetricTile(label: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(modifier) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}