package com.soshopay.data.remote

/**
 * API Configuration interface following the Dependency Inversion Principle.
 *
 * This interface defines the contract for API configuration, allowing
 * different implementations for different environments (dev, staging, production).
 *
 * Following SOLID principles:
 * - Single Responsibility: Only manages API configuration
 * - Open/Closed: Open for extension (new environments), closed for modification
 * - Liskov Substitution: Any implementation can be substituted
 * - Interface Segregation: Focused, minimal interface
 * - Dependency Inversion: Depend on abstraction, not concrete implementations
 */
interface ApiConfig {
    /**
     * The base URL for API requests
     */
    val baseUrl: String

    /**
     * The environment name (for logging and debugging)
     */
    val environmentName: String

    /**
     * Whether this is a development environment
     */
    val isDevelopment: Boolean

    /**
     * Connection timeout in milliseconds
     */
    val connectionTimeout: Long

    /**
     * Request timeout in milliseconds
     */
    val requestTimeout: Long

    /**
     * Whether to enable detailed logging
     */
    val enableLogging: Boolean
}

/**
 * Local development configuration
 *
 * Uses HTTP for local development server.
 * Network security config must allow cleartext traffic for this to work.
 */
class LocalApiConfig(
    private val localIp: String = "192.168.100.100",
    private val port: Int = 8080,
) : ApiConfig {
    override val baseUrl: String = "http://$localIp:$port/"
    override val environmentName: String = "Local Development"
    override val isDevelopment: Boolean = true
    override val connectionTimeout: Long = 30_000L // 30 seconds
    override val requestTimeout: Long = 60_000L // 60 seconds
    override val enableLogging: Boolean = true
}

/**
 * Beta/Staging environment configuration
 *
 * Uses HTTPS with proper SSL certificates
 */
class BetaApiConfig : ApiConfig {
    override val baseUrl: String = "https://beta.soshopay.com/"
    override val environmentName: String = "Beta"
    override val isDevelopment: Boolean = false
    override val connectionTimeout: Long = 30_000L
    override val requestTimeout: Long = 60_000L
    override val enableLogging: Boolean = true
}

/**
 * Production environment configuration
 *
 * Uses HTTPS with strict security settings
 */
class ProductionApiConfig : ApiConfig {
    override val baseUrl: String = "https://api.soshopay.com/"
    override val environmentName: String = "Production"
    override val isDevelopment: Boolean = false
    override val connectionTimeout: Long = 20_000L // Shorter timeout for production
    override val requestTimeout: Long = 45_000L
    override val enableLogging: Boolean = false // Disable verbose logging in production
}

/**
 * Factory for creating the appropriate API configuration
 * based on build type or environment variables
 */
object ApiConfigFactory {
    /**
     * Current active configuration
     *
     * Change this based on your current development needs:
     * - LocalApiConfig() for local development
     * - BetaApiConfig() for testing against beta server
     * - ProductionApiConfig() for production builds
     */
    fun create(environment: Environment = Environment.LOCAL): ApiConfig =
        when (environment) {
            Environment.LOCAL -> LocalApiConfig()
            Environment.BETA -> BetaApiConfig()
            Environment.PRODUCTION -> ProductionApiConfig()
        }

    /**
     * Create local config with custom IP and port
     */
    fun createLocal(
        ip: String,
        port: Int,
    ): ApiConfig = LocalApiConfig(ip, port)
}

/**
 * Supported environments
 */
enum class Environment {
    LOCAL,
    BETA,
    PRODUCTION,
}
