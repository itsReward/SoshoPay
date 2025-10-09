package com.soshopay.di

import com.soshopay.data.remote.ApiConfig
import com.soshopay.data.remote.ApiConfigFactory
import com.soshopay.data.remote.Environment
import com.soshopay.data.remote.createHttpClient
import io.ktor.client.HttpClient
import org.koin.dsl.module

/**
 * Network module for dependency injection following SOLID principles.
 *
 * This module:
 * - Provides singleton instances of network-related dependencies
 * - Separates interface from implementation
 * - Makes it easy to switch between environments
 * - Follows Dependency Inversion Principle
 *
 * IMPORTANT: Change the Environment enum value to switch between:
 * - Environment.LOCAL - For local development (HTTP)
 * - Environment.BETA - For beta testing (HTTPS)
 * - Environment.PRODUCTION - For production (HTTPS)
 */
val networkModule =
    module {

        /**
         * Provides API configuration as a singleton
         *
         * CONFIGURATION: Change this to switch environments
         * - For local development: Environment.LOCAL
         * - For beta testing: Environment.BETA
         * - For production: Environment.PRODUCTION
         */
        single<ApiConfig> {
            ApiConfigFactory.create(Environment.LOCAL)
        }

        /**
         * Provides configured HttpClient as a singleton
         *
         * The client is automatically configured based on the ApiConfig provided above.
         * This follows the Dependency Inversion Principle - the HttpClient depends
         * on the ApiConfig abstraction, not concrete implementations.
         */
        single<HttpClient> {
            createHttpClient(get())
        }
    }

/**
 * Alternative module configurations for different environments
 * Use these by replacing the networkModule in your Koin setup
 */
object NetworkModules {
    /**
     * Module for local development
     * Uses HTTP with cleartext traffic
     */
    val local =
        module {
            single<ApiConfig> { ApiConfigFactory.create(Environment.LOCAL) }
            single<HttpClient> { createHttpClient(get()) }
        }

    /**
     * Module for beta environment
     * Uses HTTPS with beta server
     */
    val beta =
        module {
            single<ApiConfig> { ApiConfigFactory.create(Environment.BETA) }
            single<HttpClient> { createHttpClient(get()) }
        }

    /**
     * Module for production environment
     * Uses HTTPS with production server and optimized settings
     */
    val production =
        module {
            single<ApiConfig> { ApiConfigFactory.create(Environment.PRODUCTION) }
            single<HttpClient> { createHttpClient(get()) }
        }

    /**
     * Module for custom local configuration
     * Useful when testing against different local servers
     */
    fun localCustom(
        ip: String,
        port: Int,
    ) = module {
        single<ApiConfig> { ApiConfigFactory.createLocal(ip, port) }
        single<HttpClient> { createHttpClient(get()) }
    }
}
