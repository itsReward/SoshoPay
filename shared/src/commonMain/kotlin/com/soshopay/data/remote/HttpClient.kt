package com.soshopay.data.remote

import com.soshopay.domain.storage.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Creates and configures an HttpClient instance with environment-specific settings.
 *
 * This function follows SOLID principles by:
 * - Single Responsibility: Only creates and configures the HTTP client
 * - Dependency Inversion: Depends on ApiConfig abstraction
 * - Open/Closed: Can be extended with new plugins without modification
 *
 * Features:
 * - Environment-aware configuration (dev/staging/production)
 * - Automatic timeout configuration
 * - Conditional logging based on environment
 * - JSON serialization with lenient parsing
 * - Proper error handling
 *
 * @param apiConfig The API configuration to use (defaults to local development)
 * @param tokenStorage The TokenStorage to retrieve auth tokens (optional)
 * @return Configured HttpClient instance
 */
fun createHttpClient(
    apiConfig: ApiConfig = ApiConfigFactory.create(Environment.LOCAL),
    tokenStorage: TokenStorage? = null,
): HttpClient {
    co.touchlab.kermit.Logger.d("HTTP_CLIENT") {
        "Creating HttpClient for ${apiConfig.environmentName} environment at ${apiConfig.baseUrl}"
    }

    return HttpClient {
        // Content negotiation for JSON serialization
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                },
            )
        }

        // Timeout configuration based on environment
        install(HttpTimeout) {
            requestTimeoutMillis = apiConfig.requestTimeout
            connectTimeoutMillis = apiConfig.connectionTimeout
            socketTimeoutMillis = apiConfig.requestTimeout
        }

        // Logging configuration - only enable in development
        if (apiConfig.enableLogging) {
            install(Logging) {
                logger =
                    object : Logger {
                        override fun log(message: String) {
                            co.touchlab.kermit.Logger
                                .d("HTTP") { message }
                        }
                    }
                level = if (apiConfig.isDevelopment) LogLevel.ALL else LogLevel.INFO
            }
        }

        // Default request configuration
        defaultRequest {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            url(apiConfig.baseUrl)

            // Add Authorization header if token is available
            if (tokenStorage != null) {
                // Use runBlocking or suspend context to get token
                // Note: This is synchronous but safe for defaultRequest
                try {
                    val token =
                        kotlinx.coroutines.runBlocking {
                            tokenStorage.getAuthToken()
                        }

                    if (token != null) {
                        header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")

                        if (apiConfig.isDevelopment) {
                            co.touchlab.kermit.Logger.d("HTTP_AUTH") {
                                "Added Authorization header with token"
                            }
                        }
                    } else {
                        if (apiConfig.isDevelopment) {
                            co.touchlab.kermit.Logger.w("HTTP_AUTH") {
                                "No token available - request will be made without Authorization header"
                            }
                        }
                    }
                } catch (e: Exception) {
                    co.touchlab.kermit.Logger.e("HTTP_AUTH") {
                        "Failed to get token: ${e.message}"
                    }
                }
            }

            // Log the request URL in development
            if (apiConfig.isDevelopment) {
                co.touchlab.kermit.Logger.d("HTTP_REQUEST") {
                    "Base URL: ${apiConfig.baseUrl}"
                }
            }
        }
    }
}

/**
 * Extension function to easily create HttpClient for specific environments
 */
fun createHttpClientForEnvironment(environment: Environment): HttpClient = createHttpClient(ApiConfigFactory.create(environment))

/**
 * Extension function to create HttpClient for local development with custom settings
 */
fun createLocalHttpClient(
    ip: String = "10.121.164.123",
    port: Int = 8080,
): HttpClient = createHttpClient(ApiConfigFactory.createLocal(ip, port))
