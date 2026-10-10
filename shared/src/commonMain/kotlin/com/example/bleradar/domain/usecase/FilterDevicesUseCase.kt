package com.example.bleradar.domain.usecase

import com.example.bleradar.domain.model.BleDevice

data class DeviceFilter(
    val query: String = "",
    val minRssi: Int? = null,
)

class FilterDevicesUseCase {
    operator fun invoke(devices: List<BleDevice>, filter: DeviceFilter): List<BleDevice> {
        val query = filter.query.trim()
        val minRssi = filter.minRssi
        return devices.filter { device ->
            val matchesSignal = minRssi == null || device.smoothedRssi >= minRssi
            val matchesQuery = query.isEmpty() ||
                    device.name?.contains(query, ignoreCase = true) == true ||
                    device.id.contains(query, ignoreCase = true)
            matchesSignal && matchesQuery
        }
    }
}