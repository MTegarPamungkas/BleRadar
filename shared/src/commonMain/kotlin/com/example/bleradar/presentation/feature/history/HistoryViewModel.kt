package com.example.bleradar.presentation.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bleradar.domain.model.DeviceHistory
import com.example.bleradar.domain.repository.DeviceHistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data class Content(val items: List<DeviceHistory>) : HistoryUiState
    data object Error : HistoryUiState
}

class HistoryViewModel(
    private val repository: DeviceHistoryRepository,
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = repository.observeHistory()
        .map<List<DeviceHistory>, HistoryUiState> { HistoryUiState.Content(it) }
        .catch { emit(HistoryUiState.Error) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState.Loading)

    fun clearHistory() {
        viewModelScope.launch { repository.clear() }
    }
}