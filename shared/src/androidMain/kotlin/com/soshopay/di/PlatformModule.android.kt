package com.soshopay.di

import com.soshopay.domain.storage.ProfileCache
import com.soshopay.domain.storage.TokenStorage
import com.soshopay.domain.storage.UserPreferences
import com.soshopay.platform.storage.AndroidProfileCache
import com.soshopay.platform.storage.AndroidTokenStorage
import com.soshopay.platform.storage.AndroidUserPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidPlatformModule =
    module {
        // Platform-specific storage implementations
        single<TokenStorage> { AndroidTokenStorage(androidContext()) }
        single<UserPreferences> { AndroidUserPreferences(androidContext()) }
        single<ProfileCache> { AndroidProfileCache(androidContext()) }
    }
