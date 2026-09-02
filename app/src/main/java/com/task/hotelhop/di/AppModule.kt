package com.task.hotelhop.di


import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.task.hotelhop.data.datasource.hotel.LocalHotelDataSource
import com.task.hotelhop.data.datasource.hotel.LocalHotelDataSourceImpl
import com.task.hotelhop.data.datasource.hotel.RemoteHotelDataSource
import com.task.hotelhop.data.datasource.hotel.RemoteHotelDataSourceImpl
import com.task.hotelhop.data.datasource.user.LocalUserDataSource
import com.task.hotelhop.data.datasource.user.LocalUserDataSourceImpl
import com.task.hotelhop.data.datasource.user.RemoteUserDataSource
import com.task.hotelhop.data.datasource.user.RemoteUserDataSourceImpl
import com.task.hotelhop.data.local.db.HotelHopDatabase
import com.task.hotelhop.data.remote.service.HotelApiService
import com.task.hotelhop.data.repo.HotelRepositoryImpl
import com.task.hotelhop.data.repo.UserRepositoryImpl
import com.task.hotelhop.domain.repo.HotelRepository
import com.task.hotelhop.domain.repo.UserRepository
import com.task.hotelhop.domain.usecase.hotel.*
import com.task.hotelhop.domain.usecase.user.*
import com.task.hotelhop.presentation.main.MainViewModel
import com.task.hotelhop.presentation.splash.SplashViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val networkModule = module {
    // Firebase
    single { FirebaseAuth.getInstance() }

    // Ktor HttpClient setup
    single {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(Logging) {
                level = LogLevel.ALL
            }
        }
    }

    // API Service
    single { HotelApiService(client = get()) }
}

val localModule = module {
    // Room Database
    single {
        Room.databaseBuilder(
            androidContext(),
            HotelHopDatabase::class.java,
            "hotel_hop_db"
        ).build()
    }

    // Room DAO
    single { get<HotelHopDatabase>().hotelDao }

    // DataStore Preferences
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create(
            produceFile = { androidContext().preferencesDataStoreFile("settings") }
        )
    }
}

val dataSourceModule = module {
    // User Data Sources
    single<RemoteUserDataSource> { RemoteUserDataSourceImpl(firebaseAuth = get()) }
    single<LocalUserDataSource> { LocalUserDataSourceImpl(dataStore = get()) }

    // Hotel Data Sources
    single<RemoteHotelDataSource> { RemoteHotelDataSourceImpl(apiService = get()) }
    single<LocalHotelDataSource> { LocalHotelDataSourceImpl(dao = get()) }
}

val repositoryModule = module {
    single<UserRepository> { UserRepositoryImpl(remoteUser = get(), localUser = get()) }
    single<HotelRepository> { HotelRepositoryImpl(remoteDS = get(), localDS = get()) }
}

val useCaseModule = module {
    // User Use Cases (Factory means a new instance is created each time it's injected)
    factory { LoginWithEmailUseCase(repository = get()) }
    factory { SignUpWithEmailUseCase(repository = get()) }
    factory { SignInWithGoogleUseCase(repository = get()) }
    factory { LogOutUseCase(repository = get()) }
    factory { GetUserDetailsUseCase(repository = get()) }
    factory { ObserveThemeUseCase(repository = get()) }
    factory { ToggleThemeUseCase(repository = get()) }
    factory { ChangeLanguageUseCase(repository = get()) }
    factory { CompleteOnboardingUseCase(repository = get()) }
    factory { CheckUserLoggedInUseCase(repository = get()) }
    factory { ObserveOnboardingUseCase(repository = get()) }
    factory { ObserveLanguageUseCase(repository = get()) }
    // Hotel Use Cases
    factory { GetPagedHotelsUseCase(repository = get()) }
    factory { GetFavoriteHotelsUseCase(repository = get()) }
    factory { GetHotelDetailsUseCase(repository = get()) }
    factory { SearchHotelsUseCase(repository = get()) }
    factory { ToggleFavoriteUseCase(repository = get()) }
}

val viewModelModule = module {
    viewModelOf(::MainViewModel)
    viewModelOf(::SplashViewModel)
}

val appModule = listOf(
    networkModule,
    localModule,
    dataSourceModule,
    repositoryModule,
    useCaseModule,
    viewModelModule
)
