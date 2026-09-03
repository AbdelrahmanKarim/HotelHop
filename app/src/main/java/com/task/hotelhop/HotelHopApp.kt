package com.task.hotelhop


import android.app.Application
import com.task.hotelhop.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class HotelHopApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@HotelHopApp)
            modules(appModule)
        }
    }
}