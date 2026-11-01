package com.iie.android.data.security

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoinMarketCapApiKeyStoreTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun encryptsCredentialAndCanReadItBack() {
        val store = CoinMarketCapApiKeyStore(context)
        val secret = "test-only-coinmarketcap-key-123456"

        store.clear()
        store.save(secret)

        assertEquals(secret, store.read())
        assertEquals(true, store.hasKey())
        val preferences = context.getSharedPreferences(
            "iie_private_credentials",
            Context.MODE_PRIVATE
        )
        val ciphertext = preferences.getString("coinmarketcap_ciphertext", null)
        assertNotNull(ciphertext)
        assertFalse(ciphertext!!.contains(secret))
    }

    @Test
    fun clearRemovesStoredCredential() {
        val store = CoinMarketCapApiKeyStore(context)
        store.save("test-only-key-to-remove")

        store.clear()

        assertEquals(null, store.read())
        assertEquals(false, store.hasKey())
    }
}
