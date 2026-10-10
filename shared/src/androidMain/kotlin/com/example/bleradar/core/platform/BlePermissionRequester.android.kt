package com.example.bleradar.core.platform

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@Composable
actual fun rememberBlePermissionRequester(
    onResult: (PermissionResult) -> Unit,
): BlePermissionRequester {
    val context = LocalContext.current
    val currentOnResult by rememberUpdatedState(onResult)
    val permissions = remember { requiredBlePermissions() }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        val activity = context.findActivity()
        val result = when {
            context.hasAll(permissions) -> PermissionResult.GRANTED
            activity != null && permissions.any {
                ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
            } -> PermissionResult.DENIED
            else -> PermissionResult.PERMANENTLY_DENIED
        }
        currentOnResult(result)
    }

    return remember(context, launcher) {
        object : BlePermissionRequester {
            override val isGranted: Boolean get() = context.hasAll(permissions)
            override fun request() = launcher.launch(permissions)
        }
    }
}

private fun requiredBlePermissions(): Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

private fun Context.hasAll(permissions: Array<String>): Boolean =
    permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}