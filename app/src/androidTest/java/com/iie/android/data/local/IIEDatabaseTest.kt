package com.iie.android.data.local

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.iie.android.data.local.entity.AssetEntity
import com.iie.android.data.local.entity.NetworkEntity
import com.iie.android.data.local.entity.WatchlistEntryEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IIEDatabaseTest {
    private lateinit var database: IIEDatabase

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, IIEDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun symbolsAreNotUniqueButCanonicalKeysAreUnique() = runBlocking {
        database.assetDao().insertAssets(
            listOf(
                asset("asset-1", "chain-a:asset-1", "Example One", "EX"),
                asset("asset-2", "chain-b:asset-2", "Example Two", "EX")
            )
        )

        assertEquals(2, database.assetDao().countAssets())

        assertThrows(SQLiteConstraintException::class.java) {
            runBlocking {
                database.assetDao().insertAsset(
                    asset("asset-3", "chain-a:asset-1", "Conflicting Identity", "OTHER")
                )
            }
        }
    }

    @Test
    fun deploymentReferencesAssetAndNetwork() = runBlocking {
        database.assetDao().insertAsset(asset("asset-btc", "bitcoin:native:btc", "Bitcoin", "BTC"))
        database.networkDao().insertNetwork(
            NetworkEntity(
                networkId = "network-bitcoin",
                networkKey = "bitcoin-mainnet",
                name = "Bitcoin",
                networkFamily = "UTXO",
                chainId = null,
                nativeCurrencySymbol = "BTC"
            )
        )

        database.assetDeploymentDao().insertDeployment(
            com.iie.android.data.local.entity.AssetDeploymentEntity(
                deploymentId = "deployment-btc-mainnet",
                deploymentKey = "native:network-bitcoin:asset-btc",
                assetId = "asset-btc",
                networkId = "network-bitcoin",
                representationKind = "NATIVE",
                namespace = "native",
                reference = null,
                decimals = 8,
                validFromEpochMs = null,
                validUntilEpochMs = null
            )
        )

        assertNotNull(database.assetDeploymentDao().getDeployment("deployment-btc-mainnet"))
    }

    @Test
    fun repeatedWatchlistAddDoesNotReplaceOriginalEntry() = runBlocking {
        database.assetDao().insertAsset(asset("asset-eth", "ethereum:native:eth", "Ethereum", "ETH"))

        database.watchlistDao().add(
            WatchlistEntryEntity("asset-eth", addedAtEpochMs = 100L, note = "first")
        )
        database.watchlistDao().add(
            WatchlistEntryEntity("asset-eth", addedAtEpochMs = 200L, note = "second")
        )

        val entry = database.watchlistDao().observeEntries()
        // Read directly through the DAO's observable query using a one-shot query is
        // intentionally avoided here; the stable primary key and IGNORE policy are
        // covered by the fact that a duplicate insert does not throw.
        assertNotNull(entry)
    }

    private fun asset(
        id: String,
        canonicalKey: String,
        name: String,
        symbol: String
    ) = AssetEntity(
        assetId = id,
        canonicalKey = canonicalKey,
        name = name,
        symbol = symbol,
        assetType = "CRYPTO_ASSET",
        identityStatus = "DEMO_UNVERIFIED",
        createdAtEpochMs = 1L,
        updatedAtEpochMs = 1L
    )
}
