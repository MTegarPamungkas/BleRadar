package com.example.bleradar.data.repository

import com.example.bleradar.data.local.DeviceHistoryDao
import com.example.bleradar.data.local.DeviceHistoryEntity
import com.example.bleradar.domain.model.BleDevice
import com.example.bleradar.domain.model.DeviceHistory
import com.example.bleradar.domain.repository.DeviceHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DeviceHistoryRepositoryImpl(
    private val dao: DeviceHistoryDao,
) : DeviceHistoryRepository {

    override fun observeHistory(): Flow<List<DeviceHistory>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun record(devices: List<BleDevice>) {
        val existing = dao.findByIds(devices.map { it.id }).associateBy { it.id }
        val entities = devices.map { device ->
            val previous = existing[device.id]
            DeviceHistoryEntity(
                id = device.id,
                name = device.name ?: previous?.name,
                lastRssi = device.rssi,
                firstSeenAtMillis = previous?.firstSeenAtMillis ?: device.lastSeenAtMillis,
                lastSeenAtMillis = device.lastSeenAtMillis,
            )
        }
        dao.upsertAll(entities)
    }

    override suspend fun clear() = dao.clear()
}

private fun DeviceHistoryEntity.toDomain() = DeviceHistory(
    id = id,
    name = name,
    lastRssi = lastRssi,
    firstSeenAtMillis = firstSeenAtMillis,
    lastSeenAtMillis = lastSeenAtMillis,
)