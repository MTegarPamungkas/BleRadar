package com.example.bleradar.domain.repository

import com.example.bleradar.domain.model.BleDevice
import com.example.bleradar.domain.model.DeviceHistory
import kotlinx.coroutines.flow.Flow

interface DeviceHistoryRepository {
    fun observeHistory(): Flow<List<DeviceHistory>>
    suspend fun record(devices: List<BleDevice>)
    suspend fun clear()
}