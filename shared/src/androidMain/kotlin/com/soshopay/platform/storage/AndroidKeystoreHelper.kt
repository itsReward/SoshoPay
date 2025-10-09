package com.soshopay.platform.storage

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import android.util.Base64
import androidx.annotation.RequiresApi
import com.soshopay.domain.util.Logger
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Enhanced AndroidKeystoreHelper with robust fallback strategy for StrongBox unavailability.
 *
 * This class provides secure encryption and decryption utilities using the Android Keystore system
 * with intelligent fallback mechanisms when hardware-backed security is unavailable.
 *
 * Features:
 * - Primary: Hardware-backed StrongBox security (API 28+)
 * - Fallback: Software-backed Android Keystore security
 * - Graceful degradation with comprehensive error handling
 * - Automatic retry mechanism with different security configurations
 * - Comprehensive logging for security audit trail
 *
 * The fallback strategy ensures maximum compatibility across devices while maintaining
 * the highest possible security level available on each device.
 */
class AndroidKeystoreHelper {
    companion object {
        private const val KEY_ALIAS = "SoshoPaySecretKey"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_LENGTH = 12 // 96 bits for GCM
        private const val TAG_LENGTH = 128 // 128 bits authentication tag
    }

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
            load(null)
        }
    }

    private var isStrongBoxBacked: Boolean = false
    private var keyCreationAttempts: Int = 0

    init {
        createKeyIfNotExists()
    }

    /**
     * Creates a secret key with fallback strategy if it doesn't exist.
     *
     * Strategy:
     * 1. Try StrongBox-backed key (API 28+, hardware support required)
     * 2. Fallback to software-backed Android Keystore
     * 3. Log security level achieved for audit purposes
     */
    private fun createKeyIfNotExists() {
        try {
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                createSecretKeyWithFallback()
                Logger.d(
                    "Secret key created successfully - StrongBox: $isStrongBoxBacked",
                    "KEYSTORE",
                )
            } else {
                // Verify existing key is accessible
                verifyExistingKey()
                Logger.d("Using existing secret key from Android Keystore", "KEYSTORE")
            }
        } catch (e: Exception) {
            Logger.e("Critical failure in keystore initialization", "KEYSTORE", e)
            throw SecurityException("Unable to initialize secure storage", e)
        }
    }

    /**
     * Attempts to create a secret key with intelligent fallback strategy.
     *
     * This method implements the Single Responsibility Principle by focusing solely
     * on key creation with different security configurations.
     */
    private fun createSecretKeyWithFallback() {
        keyCreationAttempts = 0

        // Strategy 1: Try StrongBox-backed key first (API 28+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (tryCreateKeyWithStrongBox()) {
                isStrongBoxBacked = true
                Logger.i("StrongBox-backed key created successfully", "KEYSTORE")
                return
            }
        }

        // Strategy 2: Fallback to software-backed key
        if (tryCreateKeyWithoutStrongBox()) {
            isStrongBoxBacked = false
            Logger.i("Software-backed key created successfully", "KEYSTORE")
            return
        }

        // Strategy 3: Basic key creation (minimal security)
        if (tryCreateBasicKey()) {
            isStrongBoxBacked = false
            Logger.w("Basic key created - reduced security level", "KEYSTORE")
            return
        }

        throw SecurityException("Failed to create secret key with any configuration")
    }

    /**
     * Attempts to create a StrongBox-backed key (API 28+).
     *
     * @return true if successful, false if StrongBox is unavailable
     */
    @RequiresApi(Build.VERSION_CODES.P)
    private fun tryCreateKeyWithStrongBox(): Boolean =
        try {
            keyCreationAttempts++
            Logger.d("Attempting StrongBox-backed key creation (attempt $keyCreationAttempts)", "KEYSTORE")

            val keyGenParameterSpec = buildKeyGenSpec(useStrongBox = true, enhanced = true)
            createKeyWithSpec(keyGenParameterSpec)
            true
        } catch (e: StrongBoxUnavailableException) {
            Logger.w("StrongBox unavailable - will fallback to software keystore", "KEYSTORE")
            false
        } catch (e: Exception) {
            Logger.w("StrongBox key creation failed: ${e.message}", "KEYSTORE")
            false
        }

    /**
     * Attempts to create a software-backed key with enhanced security features.
     *
     * @return true if successful, false otherwise
     */
    private fun tryCreateKeyWithoutStrongBox(): Boolean =
        try {
            keyCreationAttempts++
            Logger.d("Attempting software-backed key creation (attempt $keyCreationAttempts)", "KEYSTORE")

            val keyGenParameterSpec = buildKeyGenSpec(useStrongBox = false, enhanced = true)
            createKeyWithSpec(keyGenParameterSpec)
            true
        } catch (e: Exception) {
            Logger.w("Software-backed key creation failed: ${e.message}", "KEYSTORE")
            false
        }

    /**
     * Attempts to create a basic key with minimal security requirements.
     *
     * @return true if successful, false otherwise
     */
    private fun tryCreateBasicKey(): Boolean =
        try {
            keyCreationAttempts++
            Logger.d("Attempting basic key creation (attempt $keyCreationAttempts)", "KEYSTORE")

            val keyGenParameterSpec = buildKeyGenSpec(useStrongBox = false, enhanced = false)
            createKeyWithSpec(keyGenParameterSpec)
            true
        } catch (e: Exception) {
            Logger.e("Basic key creation failed: ${e.message}", "KEYSTORE", e)
            false
        }

    /**
     * Builds KeyGenParameterSpec with configurable security options.
     *
     * This method follows the Open/Closed Principle by allowing different
     * security configurations without modifying the core key creation logic.
     */
    private fun buildKeyGenSpec(
        useStrongBox: Boolean,
        enhanced: Boolean,
    ): KeyGenParameterSpec =
        KeyGenParameterSpec
            .Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            ).apply {
                setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                setKeySize(256)

                if (enhanced && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    setUserAuthenticationRequired(false)
                    setRandomizedEncryptionRequired(true)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        setInvalidatedByBiometricEnrollment(false)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        if (useStrongBox) {
                            setIsStrongBoxBacked(true)
                        }
                        setUnlockedDeviceRequired(false)
                    }
                }
            }.build()

    /**
     * Creates a key using the provided KeyGenParameterSpec.
     */
    private fun createKeyWithSpec(keyGenParameterSpec: KeyGenParameterSpec) {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    /**
     * Verifies that an existing key is accessible and functional.
     */
    private fun verifyExistingKey() {
        try {
            val key = getSecretKey()
            // Test encryption/decryption with the existing key
            val testData = "keystore_test"
            val encrypted = encrypt(testData)
            val decrypted = decrypt(encrypted)

            if (testData != decrypted) {
                Logger.w("Existing key verification failed - will recreate", "KEYSTORE")
                deleteKey()
                createSecretKeyWithFallback()
            }
        } catch (e: Exception) {
            Logger.w("Existing key is corrupted - will recreate: ${e.message}", "KEYSTORE")
            deleteKey()
            createSecretKeyWithFallback()
        }
    }

    private fun getSecretKey(): SecretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey

    /**
     * Encrypts plaintext using AES/GCM with the stored secret key.
     *
     * @param plainText The text to encrypt
     * @return Base64-encoded string containing IV + encrypted data
     * @throws SecurityException if encryption fails
     */
    fun encrypt(plainText: String): String =
        try {
            val secretKey = getSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            // Combine IV + encrypted data
            val combined = iv + encryptedBytes
            Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            Logger.e("Encryption failed", "KEYSTORE", e)
            throw SecurityException("Encryption failed", e)
        }

    /**
     * Decrypts a Base64-encoded string using AES/GCM with the stored secret key.
     *
     * @param encryptedData Base64-encoded string containing IV + encrypted data
     * @return Decrypted plaintext
     * @throws SecurityException if decryption fails
     */
    fun decrypt(encryptedData: String): String =
        try {
            val combined = Base64.decode(encryptedData, Base64.DEFAULT)

            // Extract IV and encrypted data
            val iv = combined.sliceArray(0 until IV_LENGTH)
            val encryptedBytes = combined.sliceArray(IV_LENGTH until combined.size)

            val secretKey = getSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Logger.e("Decryption failed", "KEYSTORE", e)
            throw SecurityException("Decryption failed", e)
        }

    /**
     * Deletes the secret key from the Android Keystore.
     */
    fun deleteKey() {
        try {
            keyStore.deleteEntry(KEY_ALIAS)
            Logger.d("Secret key deleted from Android Keystore", "KEYSTORE")
        } catch (e: Exception) {
            Logger.e("Failed to delete secret key", "KEYSTORE", e)
        }
    }

    /**
     * Checks if the secret key is available in the Android Keystore.
     *
     * @return true if the key exists and is accessible, false otherwise
     */
    fun isKeyAvailable(): Boolean =
        try {
            keyStore.containsAlias(KEY_ALIAS) && getSecretKey() != null
        } catch (e: Exception) {
            Logger.e("Failed to check key availability", "KEYSTORE", e)
            false
        }

    /**
     * Returns information about the security level of the current key.
     *
     * @return Security info for debugging and audit purposes
     */
    fun getSecurityInfo(): Map<String, Any> =
        mapOf(
            "strongBoxBacked" to isStrongBoxBacked,
            "keyExists" to isKeyAvailable(),
            "apiLevel" to Build.VERSION.SDK_INT,
            "creationAttempts" to keyCreationAttempts,
            "keystoreProvider" to KEYSTORE_PROVIDER,
        )
}
