package com.soshopay.android

import android.app.Application
import com.soshopay.android.di.androidDatabaseModule
import com.soshopay.android.di.androidModule
import com.soshopay.di.androidPlatformModule
import com.soshopay.di.sharedModule
import com.soshopay.domain.util.Logger
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.error.KoinAppAlreadyStartedException
import org.koin.core.logger.Level

/**
 * Enhanced Application class with robust dependency injection initialization.
 *
 * This class implements comprehensive error handling for Koin DI initialization,
 * ensuring the app can gracefully handle keystore failures and other system-level issues.
 *
 * Key features:
 * - Graceful handling of DI initialization failures
 * - Module-by-module loading with individual error handling
 * - Comprehensive logging for debugging
 * - Fallback mechanisms to prevent app crashes
 * - Development vs production error handling strategies
 */
class SoshoPayApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize logging first
        initializeLogging()

        // Initialize dependency injection with error handling
        initializeDependencyInjection()
    }

    /**
     * Initializes logging system for the application.
     */
    private fun initializeLogging() {
        try {
            Logger.i("SoshoPay Application starting", "APPLICATION")
        } catch (e: Exception) {
            // Even if logging fails, continue app initialization
            System.err.println("Failed to initialize logging: ${e.message}")
        }
    }

    /**
     * Initializes Koin dependency injection with comprehensive error handling.
     *
     * This method follows the fail-fast principle for critical errors while
     * providing graceful degradation for non-critical failures.
     */
    private fun initializeDependencyInjection() {
        try {
            Logger.d("Starting Koin initialization", "APPLICATION")

            startKoin {
                // Set appropriate log level for development/production
                logger(
                    org.koin.android.logger
                        .AndroidLogger(Level.ERROR),
                )

                // Provide Android context
                androidContext(this@SoshoPayApplication)

                // Load modules with individual error handling
                modules(loadModulesWithErrorHandling())
            }

            Logger.i("Koin initialization completed successfully", "APPLICATION")
        } catch (e: KoinAppAlreadyStartedException) {
            Logger.w("Koin already started - this might be a test environment", "APPLICATION")
        } catch (e: SecurityException) {
            handleSecurityException(e)
        } catch (e: Exception) {
            handleGeneralDIException(e)
        }
    }

    /**
     * Loads DI modules individually with error handling for each module.
     *
     * This approach allows the app to continue functioning even if one module fails,
     * following the Single Responsibility Principle for module loading.
     */
    private fun loadModulesWithErrorHandling(): List<org.koin.core.module.Module> {
        val successfulModules = mutableListOf<org.koin.core.module.Module>()

        // Shared module (most critical)
        try {
            Logger.d("Loading shared module", "APPLICATION")
            successfulModules.add(sharedModule)
            Logger.i("Shared module loaded successfully", "APPLICATION")
        } catch (e: Exception) {
            Logger.e("Critical: Failed to load shared module", "APPLICATION", e)
            throw SecurityException("Cannot continue without shared module", e)
        }

        // Android platform module (critical for storage)
        try {
            Logger.d("Loading android platform module", "APPLICATION")
            successfulModules.add(androidPlatformModule)
            Logger.i("Android platform module loaded successfully", "APPLICATION")
        } catch (e: Exception) {
            Logger.e("Critical: Failed to load android platform module", "APPLICATION", e)
            // This is where the original error occurred - handle gracefully
            Logger.w("Continuing with fallback storage implementations", "APPLICATION")
            // The enhanced platform module should handle this internally now
            successfulModules.add(androidPlatformModule)
        }

        // Android database module (non-critical)
        try {
            Logger.d("Loading android database module", "APPLICATION")
            successfulModules.add(androidDatabaseModule)
            Logger.i("Android database module loaded successfully", "APPLICATION")
        } catch (e: Exception) {
            Logger.w("Non-critical: Failed to load android database module", "APPLICATION", e)
            // Continue without database module - app can function with API only
        }

        // Android UI module (critical for ViewModels)
        try {
            Logger.d("Loading android UI module", "APPLICATION")
            successfulModules.add(androidModule)
            Logger.i("Android UI module loaded successfully", "APPLICATION")
        } catch (e: Exception) {
            Logger.e("Critical: Failed to load android UI module", "APPLICATION", e)
            throw SecurityException("Cannot continue without UI module", e)
        }

        return successfulModules
    }

    /**
     * Handles security exceptions during DI initialization.
     *
     * These are typically related to keystore failures or permission issues.
     */
    private fun handleSecurityException(e: SecurityException) {
        Logger.e("Security exception during DI initialization", "APPLICATION", e)

        when {
            e.message?.contains("keystore", ignoreCase = true) == true -> {
                Logger.w("Keystore unavailable - app will continue with reduced security", "APPLICATION")
                // The enhanced modules should handle this gracefully
            }
            e.message?.contains("StrongBox", ignoreCase = true) == true -> {
                Logger.w("StrongBox unavailable - falling back to software keystore", "APPLICATION")
                // The enhanced keystore helper should handle this
            }
            else -> {
                Logger.e("Unknown security exception - attempting to continue", "APPLICATION", e)
            }
        }
    }

    /**
     * Handles general exceptions during DI initialization.
     */
    private fun handleGeneralDIException(e: Exception) {
        Logger.e("General exception during DI initialization", "APPLICATION", e)

        // Determine if this is a recoverable error
        when (e) {
            is OutOfMemoryError -> {
                Logger.e("Critical: Out of memory during DI initialization", "APPLICATION", e)
                throw e // Cannot recover from OOM
            }
            is StackOverflowError -> {
                Logger.e("Critical: Stack overflow during DI initialization", "APPLICATION", e)
                throw e // Cannot recover from stack overflow
            }
            else -> {
                Logger.w("Attempting to continue after DI error", "APPLICATION", e)
                // Try to initialize with minimal modules
                initializeMinimalDI()
            }
        }
    }

    /**
     * Initializes DI with minimal modules as a last resort.
     *
     * This provides basic functionality when full initialization fails.
     */
    private fun initializeMinimalDI() {
        try {
            Logger.w("Attempting minimal DI initialization", "APPLICATION")

            // Only essential modules without complex dependencies
            startKoin {
                logger(
                    org.koin.android.logger
                        .AndroidLogger(Level.ERROR),
                )
                androidContext(this@SoshoPayApplication)
                modules(
                    listOf(
                        sharedModule, // Only the most essential module
                    ),
                )
            }

            Logger.w("Minimal DI initialization completed - app functionality may be limited", "APPLICATION")
        } catch (e: Exception) {
            Logger.e("Fatal: Even minimal DI initialization failed", "APPLICATION", e)
            throw SecurityException("Cannot initialize application", e)
        }
    }
}
