package com.example.bleradar.core.platform

import androidx.compose.runtime.Composable

enum class PermissionResult { GRANTED, DENIED, PERMANENTLY_DENIED }

interface BlePermissionRequester {
    val isGranted: Boolean
    fun request()
}

@Composable
expect fun rememberBlePermissionRequester(
    onResult: (PermissionResult) -> Unit,
): BlePermissionRequester