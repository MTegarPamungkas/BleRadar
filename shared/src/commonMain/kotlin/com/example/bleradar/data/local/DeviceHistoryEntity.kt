package com.example.bleradar.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_history")
data class DeviceHistoryEntity(
    @PrimaryKey val id: String,
    val name: String?,
    val lastRssi: Int,
    val firstSeenAtMillis: Long,
    val lastSeenAtMillis: Long,
)