package com.iie.android.data.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Stores the CoinMarketCap credential encrypted with an AES key held by Android Keystore.
 * The plaintext key is never written to disk or included in application resources/build config.
 */
class CoinMarketCapApiKeyStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    fun hasKey(): Boolean =
        preferences.contains(CIPHERTEXT_KEY) && preferences.contains(IV_KEY)

    fun save(apiKey: String) {
        require(apiKey.isNotBlank()) { "La API key no puede estar vacía." }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val ciphertext = cipher.doFinal(apiKey.trim().toByteArray(Charsets.UTF_8))
        val committed = preferences.edit()
            .putString(IV_KEY, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .putString(CIPHERTEXT_KEY, Base64.encodeToString(ciphertext, Base64.NO_WRAP))
            .commit()
        check(committed) { "No se pudo guardar la credencial cifrada." }
    }

    fun read(): String? {
        val encodedIv = preferences.getString(IV_KEY, null) ?: return null
        val encodedCiphertext = preferences.getString(CIPHERTEXT_KEY, null) ?: return null
        val iv = Base64.decode(encodedIv, Base64.NO_WRAP)
        val ciphertext = Base64.decode(encodedCiphertext, Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateSecretKey(),
            GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        )
        return cipher.doFinal(ciphertext).toString(Charsets.UTF_8)
    }

    fun clear() {
        val committed = preferences.edit()
            .remove(IV_KEY)
            .remove(CIPHERTEXT_KEY)
            .commit()
        check(committed) { "No se pudo eliminar la credencial guardada." }
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEY_STORE
        )
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    private companion object {
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val KEY_ALIAS = "iie_coinmarketcap_api_key"
        const val PREFERENCES_NAME = "iie_private_credentials"
        const val IV_KEY = "coinmarketcap_iv"
        const val CIPHERTEXT_KEY = "coinmarketcap_ciphertext"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_LENGTH_BITS = 128
    }
}
