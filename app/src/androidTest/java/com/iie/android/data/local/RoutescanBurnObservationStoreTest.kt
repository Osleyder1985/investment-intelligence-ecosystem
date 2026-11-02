package com.iie.android.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.iie.android.data.local.entity.AssetDeploymentEntity
import com.iie.android.data.local.entity.NetworkEntity
import com.iie.android.data.repository.RoutescanBurnObservationStore
import com.iie.android.data.repository.RoutescanBurnStoreResult
import com.iie.android.domain.valuation.VerifiedErc20BurnEvent
import java.math.BigDecimal
import java.math.BigInteger
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoutescanBurnObservationStoreTest {
    private lateinit var database: IIEDatabase
    private val dbName = "routescan-burn-test-${UUID.randomUUID()}.db"
    private val contract = "0x00000000000000000000000000000000000000aa"

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

    @Test fun requiresRegisteredCanonicalDeploymentAndPersistsLinkedEvidence() = runBlocking {
        InitialAssetCatalog(database) { 1_762_200_000_000L }.synchronize()
        database.networkDao().insertNetwork(
            NetworkEntity("network:test-evm", "test-evm", "Test EVM", "EVM", "1", "TST")
        )
        database.assetDeploymentDao().insertDeployment(
            AssetDeploymentEntity(
                deploymentId = "deployment:test-token",
                deploymentKey = "test-token",
                assetId = "asset:ethereum",
                networkId = "network:test-evm",
                representationKind = "ERC20",
                namespace = "EVM",
                reference = contract,
                decimals = 2,
                validFromEpochMs = null,
                validUntilEpochMs = null
            )
        )
        val event = VerifiedErc20BurnEvent(
            assetId = "asset:ethereum",
            chainId = 1,
            tokenContract = contract,
            transactionHash = "0x" + "a".repeat(64),
            blockNumber = 100,
            logIndex = 2,
            observedAtEpochMs = 1_762_199_000_000L,
            rawAmount = BigInteger("100"),
            tokenAmount = BigDecimal("1.00"),
            tokenDecimals = 2,
            sourceUrl = "https://api.routescan.io/v2/network/mainnet/evm/1/etherscan/api"
        )
        val result = RoutescanBurnObservationStore(database) { 1_762_200_000_000L }
            .persist("asset:ethereum", listOf(event))
        assertTrue(result is RoutescanBurnStoreResult.Persisted)
        assertEquals(1, (result as RoutescanBurnStoreResult.Persisted).inserted)
        val row = database.assetObservationDao().observeForAsset("asset:ethereum").first().single()
        assertEquals("deployment:test-token", row.deploymentId)
        assertEquals("ONCHAIN_ERC20_BURN_EVENT", row.observationType)
        assertEquals("QUARANTINED", row.status)
        assertEquals("ATTRIBUTION_REQUIRED", database.dataSourceDao()
            .getSource(RoutescanBurnObservationStore.SOURCE_ID)?.permittedUseStatus)
    }

    @Test fun refusesEventWithoutCanonicalContractMapping() = runBlocking {
        InitialAssetCatalog(database) { 1_762_200_000_000L }.synchronize()
        val event = VerifiedErc20BurnEvent(
            assetId = "asset:ethereum", chainId = 1, tokenContract = contract,
            transactionHash = "0x" + "b".repeat(64), blockNumber = 101, logIndex = 0,
            observedAtEpochMs = 1_762_199_000_000L, rawAmount = BigInteger.ONE,
            tokenAmount = BigDecimal.ONE, tokenDecimals = 18,
            sourceUrl = "https://api.routescan.io/"
        )
        val result = RoutescanBurnObservationStore(database) { 1_762_200_000_000L }
            .persist("asset:ethereum", listOf(event))
        assertEquals(
            listOf("TOKEN_CONTRACT_NOT_REGISTERED_FOR_CANONICAL_ASSET"),
            (result as RoutescanBurnStoreResult.Abstained).blockers
        )
    }
}
