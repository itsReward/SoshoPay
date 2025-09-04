package com.soshopay.di

import com.soshopay.data.remote.createHttpClient
import org.koin.dsl.module

val sharedModule = module {
    // Network
    single { createHttpClient() }
    
    // Add other dependencies here as you build them
}
