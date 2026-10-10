package com.example.bleradar.presentation.common

import com.example.bleradar.domain.model.BleDevice
import com.example.bleradar.domain.model.SignalZone
import kotlin.math.round
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal fun Double.format1(): String = (round(this * 10) / 10).toString()

internal fun BleDevice.distanceLabel(): String =
    if (zone == SignalZone.LOST) "Di luar jangkauan" else "≈ ${distanceMeters.format1()} m"

internal fun Long.toDisplayDateTime(): String {
    val dateTime = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dateTime.date} ${dateTime.hour.pad2()}:${dateTime.minute.pad2()}:${dateTime.second.pad2()}"
}

private fun Int.pad2(): String = toString().padStart(2, '0')