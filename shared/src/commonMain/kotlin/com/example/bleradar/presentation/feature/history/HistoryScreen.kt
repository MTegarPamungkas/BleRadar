package com.example.bleradar.presentation.feature.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bleradar.domain.model.DeviceHistory
import com.example.bleradar.presentation.common.toDisplayDateTime
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HistoryRoute(onBack: () -> Unit) {
    val viewModel = koinViewModel<HistoryViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryScreen(state = state, onBack = onBack, onClear = viewModel::clearHistory)
}

@Composable
fun HistoryScreen(state: HistoryUiState, onBack: () -> Unit, onClear: () -> Unit) {
    var confirmClear by rememberSaveable { mutableStateOf(false) }
    val hasItems = (state as? HistoryUiState.Content)?.items?.isNotEmpty() == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Perangkat") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Kembali") } },
                actions = {
                    if (hasItems) {
                        TextButton(onClick = { confirmClear = true }) { Text("Hapus") }
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (state) {
                HistoryUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                HistoryUiState.Error -> Text("Gagal memuat riwayat", Modifier.align(Alignment.Center))
                is HistoryUiState.Content -> if (state.items.isEmpty()) {
                    Text("Belum ada perangkat yang terdeteksi", Modifier.align(Alignment.Center))
                } else {
                    LazyColumn {
                        items(state.items, key = { it.id }) { HistoryItem(it) }
                    }
                }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Hapus riwayat?") },
            text = { Text("Semua riwayat perangkat akan dihapus dari penyimpanan lokal.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmClear = false
                    onClear()
                }) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Batal") }
            },
        )
    }
}

@Composable
private fun HistoryItem(item: DeviceHistory) {
    ListItem(
        headlineContent = { Text(item.name ?: "Perangkat tanpa nama") },
        supportingContent = {
            Text("${item.id}\nTerakhir terlihat: ${item.lastSeenAtMillis.toDisplayDateTime()}")
        },
        trailingContent = { Text("${item.lastRssi} dBm") },
    )
}