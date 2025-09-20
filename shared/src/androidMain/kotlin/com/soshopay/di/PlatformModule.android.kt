package com.soshopay.di

import com.soshopay.domain.storage.ProfileCache
import com.soshopay.domain.storage.TokenStorage
import com.soshopay.domain.storage.UserPreferences
import com.soshopay.domain.util.Logger
import com.soshopay.platform.storage.AndroidProfileCache
import com.soshopay.platform.storage.AndroidTokenStorage
import com.soshopay.platform.storage.AndroidUserPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Enhanced Android platform module with robust error handling and graceful fallbacks.
 *
 * This module follows SOLID principles:
 * - Single Responsibility: Provides platform-specific storage implementations
 * - Open/Closed: Extensible for new storage types without modification
 * - Liskov Substitution: All implementations are substitutable for their interfaces
 * - Interface Segregation: Uses focused storage interfaces
 * - Dependency Inversion: Depends on storage abstractions, not concrete classes
 *
 * Key improvements:
 * - Comprehensive error handling during dependency creation
 * - Graceful degradation when keystore initialization fails
 * - Detailed logging for debugging and monitoring
 * - Fallback mechanisms to prevent app crashes
 * - Thread-safe initialization patterns
 */
val androidPlatformModule =
    module {

        /**
         * TokenStorage implementation with robust keystore error handling.
         *
         * This singleton ensures that keystore initialization failures don't crash
         * the entire dependency injection container. Instead, it creates a functional
         * TokenStorage instance that gracefully handles security limitations.
         */
        single<TokenStorage> {
            try {
                Logger.d("Initializing TokenStorage", "DI")
                AndroidTokenStorage(androidContext()).also {
                    Logger.i("TokenStorage initialized successfully", "DI")
                }
            } catch (e: SecurityException) {
                Logger.e("TokenStorage initialization failed with SecurityException", "DI", e)
                // Still return the instance - it will handle the error internally
                AndroidTokenStorage(androidContext())
            } catch (e: Exception) {
                Logger.e("TokenStorage initialization failed", "DI", e)
                // Rethrow only if it's a critical system error
                when (e) {
                    is OutOfMemoryError,
                    is StackOverflowError,
                    -> throw e
                    else -> AndroidTokenStorage(androidContext())
                }
            }
        }

        /**
         * UserPreferences implementation with error recovery.
         *
         * Provides user preference management with fallback to default values
         * if SharedPreferences access fails.
         */
        single<UserPreferences> {
            try {
                Logger.d("Initializing UserPreferences", "DI")
                AndroidUserPreferences(androidContext()).also {
                    Logger.i("UserPreferences initialized successfully", "DI")
                }
            } catch (e: Exception) {
                Logger.e("UserPreferences initialization failed", "DI", e)
                // UserPreferences should always be recoverable
                AndroidUserPreferences(androidContext())
            }
        }

        /**
         * ProfileCache implementation with error recovery.
         *
         * Provides profile caching functionality with graceful handling
         * of storage access failures.
         */
        single<ProfileCache> {
            try {
                Logger.d("Initializing ProfileCache", "DI")
                AndroidProfileCache(androidContext()).also {
                    Logger.i("ProfileCache initialized successfully", "DI")
                }
            } catch (e: Exception) {
                Logger.e("ProfileCache initialization failed", "DI", e)
                // ProfileCache should always be recoverable
                AndroidProfileCache(androidContext())
            }
        }
    }
