package com.iie.android.data.market

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** AES-GCM encrypted credential storage backed by an Android Keystore key. */
class CoinMarketCapApiKeyStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME, Context.MODE_PRIVATE
    )
    @Synchronized fun save(apiKey: String) {
        val normalized = apiKey.trim()
        require(normalized.isNotEmpty()) { "La API key no puede estar vacía" }
        require(normalized.length <= 256 && normalized.none { it.isWhitespace() }) { "El formato de la API key no es válido" }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val ciphertext = cipher.doFinal(normalized.toByteArray(Charsets.UTF_8))
        val stored = Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + ":" +
            Base64.encodeToString(ciphertext, Base64.NO_WRAP)
        check(preferences.edit().putString(ENCRYPTED_KEY, stored).commit()) { "No se pudo guardar la credencial cifrada" }
    }
    @Synchronized fun read(): String? {
        val stored = preferences.getString(ENCRYPTED_KEY, null) ?: return null
        val parts = stored.split(":", limit = 2)
        check(parts.size == 2) { "La credencial cifrada está dañada" }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, Base64.decode(parts[0], Base64.NO_WRAP)))
        return cipher.doFinal(Base64.decode(parts[1], Base64.NO_WRAP)).toString(Charsets.UTF_8)
    }
    fun hasKey(): Boolean = !preferences.getString(ENCRYPTED_KEY, null).isNullOrBlank()
    @Synchronized fun delete() {
        check(preferences.edit().remove(ENCRYPTED_KEY).commit()) { "No se pudo eliminar la credencial almacenada" }
    }
    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        generator.init(KeyGenParameterSpec.Builder(KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256).setRandomizedEncryptionRequired(true).build())
        return generator.generateKey()
    }
    private companion object {
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val KEY_ALIAS = "iie_coinmarketcap_api_key_v1"
        const val PREFERENCES_NAME = "iie_secure_provider_credentials"
        const val ENCRYPTED_KEY = "coinmarketcap_api_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
