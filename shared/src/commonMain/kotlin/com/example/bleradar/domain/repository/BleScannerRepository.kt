package com.example.bleradar.domain.repository

import com.example.bleradar.domain.model.BleAdvertisement
import com.example.bleradar.domain.model.BluetoothState
import kotlinx.coroutines.flow.Flow

interface BleScannerRepository {
    fun scan(): Flow<BleAdvertisement>
    fun observeBluetoothState(): Flow<BluetoothState>
}