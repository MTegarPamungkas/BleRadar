package com.example.bleradar.core.di

import com.example.bleradar.core.common.SystemTimeProvider
import com.example.bleradar.core.common.TimeProvider
import com.example.bleradar.data.ble.KableBleScannerRepository
import com.example.bleradar.data.local.AppDatabase
import com.example.bleradar.data.local.buildDatabase
import com.example.bleradar.data.repository.DeviceHistoryRepositoryImpl
import com.example.bleradar.domain.logic.BleDeviceFactory
import com.example.bleradar.domain.logic.DistanceEstimator
import com.example.bleradar.domain.logic.RssiSmoother
import com.example.bleradar.domain.repository.BleScannerRepository
import com.example.bleradar.domain.repository.DeviceHistoryRepository
import com.example.bleradar.domain.usecase.FilterDevicesUseCase
import com.example.bleradar.domain.usecase.ObserveNearbyDevicesUseCase
import com.example.bleradar.domain.usecase.TrackDeviceUseCase
import com.example.bleradar.presentation.feature.history.HistoryViewModel
import com.example.bleradar.presentation.feature.radar.RadarViewModel
import com.example.bleradar.presentation.feature.scanner.ScannerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val APP_SCOPE = "applicationScope"

val coreModule = module {
    single<TimeProvider> { SystemTimeProvider() }
    single(named(APP_SCOPE)) { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
}

val dataModule = module {
    single<BleScannerRepository> { KableBleScannerRepository(get(named(APP_SCOPE)), get()) }
    single<AppDatabase> { buildDatabase(get()) }
    single { get<AppDatabase>().deviceHistoryDao() }
    single<DeviceHistoryRepository> { DeviceHistoryRepositoryImpl(get()) }
}

val domainModule = module {
    factory { DistanceEstimator() }
    factory { RssiSmoother() }
    factoryOf(::BleDeviceFactory)
    factoryOf(::ObserveNearbyDevicesUseCase)
    factoryOf(::TrackDeviceUseCase)
    factoryOf(::FilterDevicesUseCase)
}

val presentationModule = module {
    viewModelOf(::ScannerViewModel)
    viewModel { params -> RadarViewModel(params.get(), get(), get()) }
    viewModelOf(::HistoryViewModel)
}