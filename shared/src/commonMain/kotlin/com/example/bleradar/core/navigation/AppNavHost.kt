package com.example.bleradar.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.bleradar.presentation.feature.history.HistoryRoute
import com.example.bleradar.presentation.feature.radar.RadarRoute
import com.example.bleradar.presentation.feature.scanner.ScannerRoute
import kotlinx.serialization.Serializable

sealed interface Destination {
    @Serializable
    data object Scanner : Destination

    @Serializable
    data class Radar(val deviceId: String) : Destination

    @Serializable
    data object History : Destination
}

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Destination.Scanner) {
        composable<Destination.Scanner> {
            ScannerRoute(
                onDeviceClick = { navController.navigate(Destination.Radar(it)) },
                onHistoryClick = { navController.navigate(Destination.History) },
            )
        }
        composable<Destination.Radar> { entry ->
            RadarRoute(
                deviceId = entry.toRoute<Destination.Radar>().deviceId,
                onBack = navController::navigateUp,
            )
        }
        composable<Destination.History> {
            HistoryRoute(onBack = navController::navigateUp)
        }
    }
}