package com.iie.android.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.iie.android.data.market.CoinMarketCapDailyHistoryFetcher
import com.iie.android.data.market.CoinPaprikaHistoricalPoint
import com.iie.android.data.repository.RoomAssetRepository
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoinMarketCapRoomRestartTest {

    @Test
    fun coinMarketCapHistorySurvivesRoomCloseAndReopenAndRemainsIncremental() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val databaseName = "iie-cmc-restart-${UUID.randomUUID()}.db"
        val now = 1_800_000_000_000L
        val start = LocalDate.ofEpochDay(now / 86_400_000L - 2L)
        val end = LocalDate.ofEpochDay(now / 86_400_000L - 1L)
        var database: IIEDatabase? = null

        try {
            database = Room.databaseBuilder(context, IIEDatabase::class.java, databaseName)
                .allowMainThreadQueries()
                .build()
            InitialAssetCatalog(database) { now }.synchronize()

            val initialRepository = RoomAssetRepository(database) { now }
            var initialFetchCount = 0
            val initialFetcher = CoinMarketCapDailyHistoryFetcher {
                    cmcAssetId, apiKey, requestedStart, requestedEnd, retrievedAt ->
                initialFetchCount += 1
                assertEquals(3408L, cmcAssetId)
                assertEquals("test-key-never-persisted", apiKey)
                assertEquals(start, requestedStart)
                assertEquals(end, requestedEnd)
                assertEquals(now, retrievedAt)
                listOf(
                    CoinPaprikaHistoricalPoint(
                        "3408",
                        (now / 86_400_000L - 2L) * 86_400_000L,
                        1.0001,
                        1200.0,
                        45_000_000_000.0,
                        now,
                        "source:coinmarketcap"
                    ),
                    CoinPaprikaHistoricalPoint(
                        "3408",
                        (now / 86_400_000L - 1L) * 86_400_000L,
                        0.9999,
                        1300.0,
                        44_900_000_000.0,
                        now,
                        "source:coinmarketcap"
                    )
                )
            }

            val firstSync = initialRepository.downloadAndPersistCoinMarketCapDailyHistory(
                "asset:usd-coin",
                "USDC",
                start,
                end,
                "test-key-never-persisted",
                initialFetcher
            )

            assertEquals(1, initialFetchCount)
            assertEquals(2, firstSync.pointsReceived)
            assertEquals(2, firstSync.pointsInserted)

            // Closing and rebuilding Room simulates the database lifecycle across an app restart.
            database.close()
            database = null

            database = Room.databaseBuilder(context, IIEDatabase::class.java, databaseName)
                .allowMainThreadQueries()
                .build()
            val reopenedRepository = RoomAssetRepository(database) { now + 60_000L }
            val restored = reopenedRepository.getLatestCoinMarketCapDailyHistory(
                "asset:usd-coin",
                limit = 10
            )

            assertEquals(2, restored.size)
            assertEquals(listOf(0.9999, 1.0001), restored.map { it.priceUsd })
            assertTrue(restored.all { it.sourceId == "source:coinmarketcap" })
            assertTrue(restored.all { it.providerId == "3408" })
            assertTrue(restored.all { it.retrievedAtEpochMs == now })

            var unexpectedNetworkFetch = false
            val noNetworkFetcher = CoinMarketCapDailyHistoryFetcher { _, _, _, _, _ ->
                unexpectedNetworkFetch = true
                error("Persisted CMC history should make this range a no-op after restart")
            }
            val repeatedSync = reopenedRepository.downloadAndPersistCoinMarketCapDailyHistory(
                "asset:usd-coin",
                "USDC",
                start,
                end,
                "test-key-never-persisted",
                noNetworkFetcher
            )

            assertEquals(false, unexpectedNetworkFetch)
            assertEquals(0, repeatedSync.pointsReceived)
            assertEquals(0, repeatedSync.pointsInserted)
            assertEquals(2, reopenedRepository.getLatestCoinMarketCapDailyHistory(
                "asset:usd-coin",
                limit = 10
            ).size)
        } finally {
            database?.close()
            context.deleteDatabase(databaseName)
        }
    }
}
