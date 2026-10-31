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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class IIEDatabaseTest {
    private lateinit var database: IIEDatabase
    private lateinit var context: Context
    private val databaseName = "iie-test-${UUID.randomUUID()}.db"

    @Before
    fun createDatabase() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.databaseBuilder(context, IIEDatabase::class.java, databaseName)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDatabase() {
        database.close()
        context.deleteDatabase(databaseName)
    }

    @Test
    fun symbolsAreNotUniqueButCanonicalKeysAreUnique(): Unit = runBlocking {
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
            AssetDeploymentEntity(
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
    fun observationCannotReferenceDeploymentOfAnotherAsset(): Unit = runBlocking {
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

    @Test
    fun repositoryWatchlistToggleIsAtomicAndAlternatesState() = runBlocking {
        database.assetDao().insertAsset(
            asset("asset-toggle", "canonical:toggle", "Toggle Asset", "TGL")
        )
        val repository = com.iie.android.data.repository.RoomAssetRepository(database) { 500L }

        assertEquals(true, repository.toggleWatchlist("asset-toggle"))
        assertNotNull(database.watchlistDao().getEntry("asset-toggle"))
        assertEquals(false, repository.toggleWatchlist("asset-toggle"))
        assertEquals(null, database.watchlistDao().getEntry("asset-toggle"))
    }

    @Test
    fun searchMatchesAssetNameAndSymbolWithoutAssumingUniqueSymbols() = runBlocking {
        database.assetDao().insertAssets(
            listOf(
                asset("asset-btc", "canonical:btc", "Bitcoin", "BTC"),
                asset("asset-wrapped-btc", "canonical:wbtc", "Wrapped Bitcoin", "BTC"),
                asset("asset-eth", "canonical:eth", "Ethereum", "ETH")
            )
        )

        val bySymbol = database.assetDao().observeAssets("btc").first()
        val byName = database.assetDao().observeAssets("ethereum").first()

        assertEquals(2, bySymbol.size)
        assertEquals("Ethereum", byName.single().name)
    }

    @Test
    fun deploymentDetailJoinsCorrectNetworkName() = runBlocking {
        database.assetDao().insertAsset(
            asset("asset-btc", "canonical:btc", "Bitcoin", "BTC")
        )
        database.networkDao().insertNetwork(
            NetworkEntity(
                networkId = "network-btc",
                networkKey = "bitcoin-mainnet",
                name = "Bitcoin Mainnet",
                networkFamily = "UTXO",
                chainId = null,
                nativeCurrencySymbol = "BTC"
            )
        )
        database.assetDeploymentDao().insertDeployment(
            AssetDeploymentEntity(
                deploymentId = "deployment-btc",
                deploymentKey = "native:btc",
                assetId = "asset-btc",
                networkId = "network-btc",
                representationKind = "NATIVE",
                namespace = "native",
                reference = null,
                decimals = 8,
                validFromEpochMs = null,
                validUntilEpochMs = null
            )
        )

        val detail = database.assetDeploymentDao()
            .observeSummariesForAsset("asset-btc").first().single()

        assertEquals("Bitcoin Mainnet", detail.networkName)
        assertEquals("NATIVE", detail.representationKind)
        assertEquals(8, detail.decimals)
    }

    @Test
    fun fileBackedDatabaseRetainsCatalogAndWatchlistAfterReopen() = runBlocking {
        DemoAssetSeeder(database) { 100L }.seed()
        database.watchlistDao().add(
            WatchlistEntryEntity("asset:bitcoin", addedAtEpochMs = 150L, note = "persist me")
        )

        database.close()
        database = Room.databaseBuilder(context, IIEDatabase::class.java, databaseName)
            .allowMainThreadQueries()
            .build()

        assertEquals(5, database.assetDao().countAssets())
        val entry = database.watchlistDao().getEntry("asset:bitcoin")
        assertNotNull(entry)
        assertEquals(150L, entry?.addedAtEpochMs)
        assertEquals("persist me", entry?.note)
        val provenance = database.assetObservationDao()
            .getLatest("asset:bitcoin", "IDENTITY_FIXTURE")
        assertNotNull(provenance)
        assertEquals(100L, provenance?.retrievedAtEpochMs)
        assertEquals("DEMO", provenance?.status)
    }

    @Test
    fun provenanceQueryJoinsObservationToRegisteredSource() = runBlocking {
        DemoAssetSeeder(database) { 300L }.seed()

        val observations = database.assetObservationDao()
            .observeProvenanceForAsset("asset:bitcoin").first()

        assertEquals(1, observations.size)
        assertEquals("IIE local demonstration fixture", observations.single().sourceDisplayName)
        assertEquals("LOCAL_FIXTURE", observations.single().sourceProviderType)
        assertEquals("INTERNAL_DEMO_ONLY", observations.single().sourcePermittedUseStatus)
        assertEquals("IDENTITY_FIXTURE", observations.single().observationType)
        assertEquals("DEMO", observations.single().status)
        assertEquals("UNVERIFIED", observations.single().quality)
    }

    @Test
    fun repositoryMapsPersistedObservationAndSourceUseForEvidenceEvaluation(): Unit = runBlocking {
        DemoAssetSeeder(database) { 400L }.seed()
        val repository = com.iie.android.data.repository.RoomAssetRepository(database)

        val record = repository.getLatestObservationForEvaluation(
            assetId = "asset:bitcoin",
            observationType = "IDENTITY_FIXTURE"
        )

        assertNotNull(record)
        assertEquals("observation:demo:asset:bitcoin", record?.first?.observationId)
        assertEquals("source:iie-demo-fixture", record?.first?.sourceId)
        assertEquals("UNVERIFIED", record?.first?.quality)
        assertEquals("INTERNAL_DEMO_ONLY", record?.second)
        assertEquals(
            """{"recordType":"synthetic_demo_fixture","marketData":false}""",
            record?.first?.valueJson
        )
    }

    @Test
    fun demoSeederIsIdempotentAndPreservesUserWatchlistData() = runBlocking {
        DemoAssetSeeder(database) { 100L }.seed()
        database.watchlistDao().add(
            WatchlistEntryEntity("asset:bitcoin", addedAtEpochMs = 150L, note = "user note")
        )

        DemoAssetSeeder(database) { 200L }.seed()

        assertEquals(5, database.assetDao().countAssets())
        assertNotNull(database.assetDeploymentDao().getDeployment("deployment:bitcoin-native"))
        val provenance = database.assetObservationDao()
            .getLatest("asset:bitcoin", "IDENTITY_FIXTURE")
        assertNotNull(provenance)
        assertEquals(100L, provenance?.retrievedAtEpochMs)
        assertEquals("source:iie-demo-fixture", provenance?.sourceId)
        val entry = database.watchlistDao().getEntry("asset:bitcoin")
        assertNotNull(entry)
        assertEquals(150L, entry?.addedAtEpochMs)
        assertEquals("user note", entry?.note)
    }

    @Test
    fun usdcAndUsdtHaveIndependentDemoIdentitiesAndNoAssumedDeployments() = runBlocking {
        DemoAssetSeeder(database) { 250L }.seed()
        val usdc = database.assetDao().getAsset("asset:usd-coin")
        val usdt = database.assetDao().getAsset("asset:tether")
        assertNotNull(usdc)
        assertNotNull(usdt)
        assertEquals("USDC", usdc?.symbol)
        assertEquals("USDT", usdt?.symbol)
        assertEquals("USD Coin", usdc?.name)
        assertEquals("Tether", usdt?.name)
        org.junit.Assert.assertNotEquals(usdc?.assetId, usdt?.assetId)
        org.junit.Assert.assertNotEquals(usdc?.canonicalKey, usdt?.canonicalKey)
        assertEquals(0, database.assetDeploymentDao().observeSummariesForAsset("asset:usd-coin").first().size)
        assertEquals(0, database.assetDeploymentDao().observeSummariesForAsset("asset:tether").first().size)
        val usdcIdentifier = database.assetIdentifierDao().findIdentifier("source:iie-demo-fixture", "iie-demo-fixture", "usd-coin")
        val usdtIdentifier = database.assetIdentifierDao().findIdentifier("source:iie-demo-fixture", "iie-demo-fixture", "tether")
        assertNotNull(usdcIdentifier)
        assertNotNull(usdtIdentifier)
        assertEquals("asset:usd-coin", usdcIdentifier?.assetId)
        assertEquals("asset:tether", usdtIdentifier?.assetId)
        org.junit.Assert.assertNotEquals(usdcIdentifier?.identifierValue, usdtIdentifier?.identifierValue)
    }

    @Test
    fun coinPaprikaMapsUsdcAndUsdtToDifferentProviderIds() = runBlocking {
        DemoAssetSeeder(database) { 300L }.seed()
        val usdc = database.assetIdentifierDao().findIdentifier("source:coinpaprika", "coinpaprika", "usdc-usd-coin")
        val usdt = database.assetIdentifierDao().findIdentifier("source:coinpaprika", "coinpaprika", "usdt-tether")
        assertNotNull(usdc)
        assertNotNull(usdt)
        assertEquals("asset:usd-coin", usdc?.assetId)
        assertEquals("asset:tether", usdt?.assetId)
        assertEquals("PROVIDER_ID_CONFIRMED", usdc?.verificationStatus)
        assertEquals("PROVIDER_ID_CONFIRMED", usdt?.verificationStatus)
        val source = database.dataSourceDao().getSource("source:coinpaprika")
        assertNotNull(source)
        assertEquals("PERSONAL_USE_ONLY", source?.permittedUseStatus)
    }


    @Test
    fun marketQuoteObservationsRemainSeparateForUsdcAndUsdt() = runBlocking {
        DemoAssetSeeder(database) { 1_800_000_000_000L }.seed()
        val sourceId = "source:coinpaprika"
        val dao = database.assetObservationDao()
        dao.insertObservations(
            listOf(
                AssetObservationEntity(
                    observationId = "observation:quote:usdc",
                    assetId = "asset:usd-coin",
                    deploymentId = null,
                    sourceId = sourceId,
                    observationType = "MARKET_QUOTE",
                    observedAtEpochMs = 1_799_999_900_000L,
                    retrievedAtEpochMs = 1_800_000_000_000L,
                    status = "ACCEPTED",
                    quality = "PROVIDER_SCHEMA_VALIDATED",
                    valueSchemaVersion = 1,
                    valueJson = """{"schema":"iie.market-quote.v1","providerId":"usdc-usd-coin","symbol":"USDC","quoteCurrency":"USD","price":1.0001,"providerUpdatedAtEpochMs":1799999900000,"retrievedAtEpochMs":1800000000000}""",
                    payloadSha256 = "fixture-usdc",
                    licenseReference = "https://coinpaprika.com/terms-of-use"
                ),
                AssetObservationEntity(
                    observationId = "observation:quote:usdt",
                    assetId = "asset:tether",
                    deploymentId = null,
                    sourceId = sourceId,
                    observationType = "MARKET_QUOTE",
                    observedAtEpochMs = 1_799_999_900_000L,
                    retrievedAtEpochMs = 1_800_000_000_000L,
                    status = "ACCEPTED",
                    quality = "PROVIDER_SCHEMA_VALIDATED",
                    valueSchemaVersion = 1,
                    valueJson = """{"schema":"iie.market-quote.v1","providerId":"usdt-tether","symbol":"USDT","quoteCurrency":"USD","price":0.9999,"providerUpdatedAtEpochMs":1799999900000,"retrievedAtEpochMs":1800000000000}""",
                    payloadSha256 = "fixture-usdt",
                    licenseReference = "https://coinpaprika.com/terms-of-use"
                )
            )
        )

        val usdc = dao.getLatestObservation("asset:usd-coin", "MARKET_QUOTE") ?: error("USDC quote missing")
        val usdt = dao.getLatestObservation("asset:tether", "MARKET_QUOTE") ?: error("USDT quote missing")
        assertEquals("usdc-usd-coin", org.json.JSONObject(usdc.valueJson).getString("providerId"))
        assertEquals("usdt-tether", org.json.JSONObject(usdt.valueJson).getString("providerId"))
        assertEquals("USDC", org.json.JSONObject(usdc.valueJson).getString("symbol"))
        assertEquals("USDT", org.json.JSONObject(usdt.valueJson).getString("symbol"))
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
