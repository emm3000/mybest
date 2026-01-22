package com.emm.mybest

import android.app.Application
import com.emm.mybest.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MyBestApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MyBestApp)
            modules(appModule)
        }
    }
}
