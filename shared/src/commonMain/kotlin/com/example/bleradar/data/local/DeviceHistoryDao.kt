package com.example.bleradar.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceHistoryDao {
    @Query("SELECT * FROM device_history ORDER BY lastSeenAtMillis DESC")
    fun observeAll(): Flow<List<DeviceHistoryEntity>>

    @Query("SELECT * FROM device_history WHERE id IN (:ids)")
    suspend fun findByIds(ids: List<String>): List<DeviceHistoryEntity>

    @Upsert
    suspend fun upsertAll(items: List<DeviceHistoryEntity>)

    @Query("DELETE FROM device_history")
    suspend fun clear()
}