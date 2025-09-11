package com.soshopay.platform.storage

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.soshopay.domain.util.Logger
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

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

    init {
        createKeyIfNotExists()
    }

    private fun createKeyIfNotExists() {
        try {
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                createSecretKey()
                Logger.d("Created new secret key in Android Keystore", "KEYSTORE")
            } else {
                Logger.d("Using existing secret key from Android Keystore", "KEYSTORE")
            }
        } catch (e: Exception) {
            Logger.e("Failed to create or access secret key", "KEYSTORE", e)
            throw SecurityException("Unable to initialize secure storage", e)
        }
    }

    private fun createSecretKey() {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)

        val keyGenParameterSpec =
            KeyGenParameterSpec
                .Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .apply {
                    // Enhanced security for Android 6.0+ (API 23+)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        setUserAuthenticationRequired(false) // Set to true if you want biometric/PIN protection
                        setRandomizedEncryptionRequired(true)
                    }

                    // Additional security for Android 7.0+ (API 24+)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        setInvalidatedByBiometricEnrollment(false)
                    }

                    // Hardware security for Android 9.0+ (API 28+)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        setIsStrongBoxBacked(true) // Use hardware security module if available
                        setUnlockedDeviceRequired(false) // Set to true for additional security
                    }
                }.build()

        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    private fun getSecretKey(): SecretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey

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
            Logger.e("Failed to encrypt data", "KEYSTORE", e)
            throw SecurityException("Encryption failed", e)
        }

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
            Logger.e("Failed to decrypt data", "KEYSTORE", e)
            throw SecurityException("Decryption failed", e)
        }

    fun deleteKey() {
        try {
            keyStore.deleteEntry(KEY_ALIAS)
            Logger.d("Secret key deleted from Android Keystore", "KEYSTORE")
        } catch (e: Exception) {
            Logger.e("Failed to delete secret key", "KEYSTORE", e)
        }
    }

    fun isKeyAvailable(): Boolean =
        try {
            keyStore.containsAlias(KEY_ALIAS)
        } catch (e: Exception) {
            Logger.e("Failed to check key availability", "KEYSTORE", e)
            false
        }
}
