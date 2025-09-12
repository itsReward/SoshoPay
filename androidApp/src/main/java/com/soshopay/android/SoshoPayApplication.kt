package com.soshopay.android

import android.app.Application
import com.soshopay.android.di.androidDatabaseModule
import com.soshopay.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SoshoPayApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@SoshoPayApplication)
            modules(
                sharedModule,
                androidDatabaseModule,
            )
        }
    }
}
