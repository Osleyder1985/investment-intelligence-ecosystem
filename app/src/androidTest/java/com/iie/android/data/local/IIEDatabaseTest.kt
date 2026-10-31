package com.iie.android.data.local

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.iie.android.data.local.entity.AssetEntity
import com.iie.android.data.local.entity.AssetDeploymentEntity
import com.iie.android.data.local.entity.AssetObservationEntity
import com.iie.android.data.local.entity.DataSourceEntity
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
    fun observationCannotReferenceDeploymentOfAnotherAsset() = runBlocking {
        database.assetDao().insertAssets(
            listOf(
                asset("asset-a", "canonical:a", "Asset A", "AAA"),
                asset("asset-b", "canonical:b", "Asset B", "BBB")
            )
        )
        database.networkDao().insertNetwork(
            NetworkEntity(
                networkId = "network-test",
                networkKey = "test-network",
                name = "Test Network",
                networkFamily = "ACCOUNT",
                chainId = "test:1",
                nativeCurrencySymbol = "TST"
            )
        )
        database.dataSourceDao().insertSource(
            DataSourceEntity(
                sourceId = "source-test",
                sourceKey = "test-provider",
                displayName = "Test Provider",
                providerType = "FIXTURE",
                homepageUrl = null,
                termsUrl = null,
                attributionText = null,
                permittedUseStatus = "TEST_ONLY",
                registeredAtEpochMs = 1L
            )
        )
        database.assetDeploymentDao().insertDeployment(
            AssetDeploymentEntity(
                deploymentId = "deployment-a",
                deploymentKey = "native:network-test:asset-a",
                assetId = "asset-a",
                networkId = "network-test",
                representationKind = "NATIVE",
                namespace = "native",
                reference = null,
                decimals = 8,
                validFromEpochMs = null,
                validUntilEpochMs = null
            )
        )

        assertThrows(SQLiteConstraintException::class.java) {
            runBlocking {
                database.assetObservationDao().insertObservation(
                    AssetObservationEntity(
                        observationId = "observation-invalid",
                        assetId = "asset-b",
                        deploymentId = "deployment-a",
                        sourceId = "source-test",
                        observationType = "IDENTITY_FIXTURE",
                        observedAtEpochMs = 1L,
                        retrievedAtEpochMs = 1L,
                        status = "DEMO",
                        quality = "UNVERIFIED",
                        valueSchemaVersion = 1,
                        valueJson = "{}",
                        payloadSha256 = null,
                        licenseReference = null
                    )
                )
            }
        }
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

        val entry = database.watchlistDao().getEntry("asset-eth")
        assertNotNull(entry)
        assertEquals(100L, entry?.addedAtEpochMs)
        assertEquals("first", entry?.note)
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
