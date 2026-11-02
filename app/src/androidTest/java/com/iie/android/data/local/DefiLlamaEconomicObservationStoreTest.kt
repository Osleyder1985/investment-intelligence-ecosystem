package com.iie.android.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.iie.android.data.repository.DefiLlamaEconomicObservationStore
import com.iie.android.domain.valuation.DefiLlamaDailyPoint
import com.iie.android.domain.valuation.DefiLlamaEconomicEvidenceResult
import com.iie.android.domain.valuation.DefiLlamaEconomicObservation
import com.iie.android.domain.valuation.DefiLlamaMetric
import com.iie.android.domain.valuation.DefiLlamaMetricSeries
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class DefiLlamaEconomicObservationStoreTest {
    private lateinit var database: IIEDatabase
    private val dbName = "defillama-test-${UUID.randomUUID()}.db"

    @Before fun setUp() {
        database = Room.databaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            IIEDatabase::class.java,
            dbName
        ).allowMainThreadQueries().build()
    }

    @After fun tearDown() {
        database.close()
        ApplicationProvider.getApplicationContext<Context>().deleteDatabase(dbName)
    }

    @Test fun persistsQuarantinedEvidenceAndIsIdempotent() = runBlocking {
        InitialAssetCatalog(database) { 1_762_200_000_000L }.synchronize()
        val series = DefiLlamaMetricSeries(
            metric = DefiLlamaMetric.PROTOCOL_FEES,
            protocolSlug = "aave",
            protocolName = "Aave",
            total24hUsd = 100.0,
            total48hTo24hUsd = null,
            total7dUsd = 700.0,
            totalAllTimeUsd = null,
            dailyPoints = listOf(DefiLlamaDailyPoint(1_761_955_200_000L, 100.0)),
            sourceUrl = "https://api.llama.fi/summary/fees/aave?dataType=dailyFees",
            retrievedAtEpochMs = 1_762_199_000_000L
        )
        val evidence = DefiLlamaEconomicEvidenceResult.Ready(
            listOf(
                DefiLlamaEconomicObservation(
                    assetId = "asset:ethereum",
                    providerMetric = series.metric,
                    protocolSlug = series.protocolSlug,
                    protocolName = series.protocolName,
                    period = "DAILY",
                    valueUsd = 100.0,
                    observedAtEpochMs = 1_761_955_200_000L,
                    retrievedAtEpochMs = series.retrievedAtEpochMs,
                    sourceId = DefiLlamaEconomicObservationStore.SOURCE_ID,
                    sourceUrl = series.sourceUrl,
                    evidenceStatus = "PROVIDER_AGGREGATED_PENDING_INDEPENDENT_ONCHAIN_VERIFICATION",
                    limitations = listOf("Not raw on-chain event evidence")
                )
            )
        )
        val store = DefiLlamaEconomicObservationStore(database) { 1_762_200_000_000L }

        val first = store.persist("asset:ethereum", evidence)
        val second = store.persist("asset:ethereum", evidence)

        val firstResult = first as com.iie.android.data.repository.DefiLlamaStoreResult.Persisted
        val secondResult = second as com.iie.android.data.repository.DefiLlamaStoreResult.Persisted
        assertEquals(1, firstResult.inserted)
        assertEquals(1, secondResult.duplicates)
        assertEquals("REVIEW_REQUIRED", firstResult.sourceUseStatus)
        val stored = database.assetObservationDao()
            .observeForAsset("asset:ethereum")
        val row = stored.first().single()
        assertEquals("QUARANTINED", row.status)
        assertEquals("UNVERIFIED", row.quality)
        assertNotNull(row.payloadSha256)
        assertEquals("PROTOCOL_ECONOMIC_METRIC", row.observationType)
        assertEquals("REVIEW_REQUIRED", database.dataSourceDao()
            .getSource(DefiLlamaEconomicObservationStore.SOURCE_ID)?.permittedUseStatus)
    }
}
