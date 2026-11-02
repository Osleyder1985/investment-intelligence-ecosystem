package com.iie.android.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.iie.android.data.local.entity.AssetDeploymentEntity
import com.iie.android.data.local.entity.NetworkEntity
import com.iie.android.data.repository.DistributionClaimObservationStore
import com.iie.android.data.repository.DistributionClaimStoreResult
import com.iie.android.domain.valuation.DistributionClaimDecodeResult
import com.iie.android.domain.valuation.VerifiedDistributionClaimPaymentCandidate
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
class DistributionClaimObservationStoreTest {
    private lateinit var database: IIEDatabase
    private val dbName = "distribution-claim-test-${UUID.randomUUID()}.db"
    private val token = "0x1f9840a85d5aF5bf1D1762F925BDADdC4201F984"

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

    private fun candidate() = VerifiedDistributionClaimPaymentCandidate(
        protocolId = "protocol:uniswap",
        assetId = "asset:ethereum",
        chainId = 1,
        transactionHash = "0x" + "a".repeat(64),
        blockNumber = 20_000_000,
        blockHash = "0x" + "b".repeat(64),
        distributorAddress = "0x090D4613473dEE047c3f2706764f49E0821D256e",
        beneficiaryAddress = "0x2222222222222222222222222222222222222222",
        tokenContract = token,
        rawAmount = "400000000000000000000",
        claimEventLogIndex = 4,
        paymentTransferLogIndex = 5,
        blockTimestampEpochMs = 1_762_199_000_000L
    )

    @Test fun persistsQuarantinedClaimAndIsIdempotent() = runBlocking {
        InitialAssetCatalog(database) { 1_762_200_000_000L }.synchronize()
        database.networkDao().insertNetwork(
            NetworkEntity("network:ethereum", "ethereum", "Ethereum", "EVM", "1", "ETH")
        )
        database.assetDeploymentDao().insertDeployment(
            AssetDeploymentEntity(
                deploymentId = "deployment:uni",
                deploymentKey = "uni",
                assetId = "asset:ethereum",
                networkId = "network:ethereum",
                representationKind = "ERC20",
                namespace = "EVM",
                reference = token,
                decimals = 18,
                validFromEpochMs = null,
                validUntilEpochMs = null
            )
        )
        val store = DistributionClaimObservationStore(database) { 1_762_200_000_000L }
        val evidence = DistributionClaimDecodeResult.Candidate(candidate())

        val first = store.persist(evidence) as DistributionClaimStoreResult.Persisted
        val second = store.persist(evidence) as DistributionClaimStoreResult.Persisted

        assertEquals(1, first.inserted)
        assertEquals(1, second.duplicates)
        assertEquals("REVIEW_REQUIRED", first.sourceUseStatus)
        val row = database.assetObservationDao().observeForAsset("asset:ethereum").first().single()
        assertEquals(DistributionClaimObservationStore.OBSERVATION_TYPE, row.observationType)
        assertEquals("QUARANTINED", row.status)
        assertEquals("UNVERIFIED", row.quality)
        assertTrue(row.payloadSha256?.isNotBlank() == true)
        assertEquals(
            "REVIEW_REQUIRED",
            database.dataSourceDao().getSource(DistributionClaimObservationStore.SOURCE_ID)?.permittedUseStatus
        )
    }

    @Test fun refusesClaimForUnregisteredTokenDeployment() = runBlocking {
        InitialAssetCatalog(database) { 1_762_200_000_000L }.synchronize()
        val result = DistributionClaimObservationStore(database) { 1_762_200_000_000L }
            .persist(DistributionClaimDecodeResult.Candidate(candidate()))
        assertEquals(
            listOf("DISTRIBUTED_TOKEN_NOT_REGISTERED_FOR_CANONICAL_ASSET"),
            (result as DistributionClaimStoreResult.Abstained).blockers
        )
    }
}
