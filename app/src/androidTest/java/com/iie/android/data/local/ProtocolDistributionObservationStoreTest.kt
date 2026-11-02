package com.iie.android.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.iie.android.data.local.entity.AssetDeploymentEntity
import com.iie.android.data.local.entity.NetworkEntity
import com.iie.android.data.repository.ProtocolDistributionObservationStore
import com.iie.android.data.repository.ProtocolDistributionStoreResult
import com.iie.android.domain.valuation.BatchDistributionPayment
import com.iie.android.domain.valuation.VerifiedBatchDistributionCandidate
import com.iie.android.domain.valuation.VestingWithdrawalCandidate
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
class ProtocolDistributionObservationStoreTest {
    private lateinit var database: IIEDatabase
    private val dbName = "protocol-distribution-store-${UUID.randomUUID()}.db"
    private val token = "0x1f9840a85d5aF5bf1D1762F925BDADdC4201F984"
    private val now = 1_793_000_000_000L

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

    private suspend fun registerDeployment() {
        InitialAssetCatalog(database) { now }.synchronize()
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
    }

    @Test fun persistsVestingAndBatchCandidatesAsQuarantinedAndIdempotent() = runBlocking {
        registerDeployment()
        val store = ProtocolDistributionObservationStore(database) { now }
        val vesting = VestingWithdrawalCandidate(
            protocolId = "protocol:uniswap",
            chainId = 1,
            vestingContractAddress = "0xCa046A83EDB78F74aE338bb5A291bF6FdAc9e1D2",
            recipientAddress = "0x2222222222222222222222222222222222222222",
            tokenContract = token,
            rawAmount = "1000",
            quartersPaid = 1,
            transactionHash = "0x" + "a".repeat(64),
            blockNumber = 20_000_000,
            blockHash = "0x" + "b".repeat(64),
            blockTimestampEpochMs = now - 1_000,
            withdrawalLogIndex = 4,
            transferLogIndex = 5
        )
        val batch = VerifiedBatchDistributionCandidate(
            protocolId = "protocol:uniswap",
            assetId = "asset:ethereum",
            chainId = 1,
            distributorAddress = "0x090D4613473dEE047c3f2706764f49E0821D256e",
            tokenContract = token,
            transactionHash = "0x" + "c".repeat(64),
            blockNumber = 20_000_001,
            blockHash = "0x" + "d".repeat(64),
            blockTimestampEpochMs = now - 500,
            payments = listOf(
                BatchDistributionPayment("0x3333333333333333333333333333333333333333", "100", 1),
                BatchDistributionPayment("0x4444444444444444444444444444444444444444", "200", 2)
            )
        )

        val firstVesting = store.persistVesting("asset:ethereum", vesting) as ProtocolDistributionStoreResult.Persisted
        val duplicateVesting = store.persistVesting("asset:ethereum", vesting) as ProtocolDistributionStoreResult.Persisted
        val firstBatch = store.persistBatch(batch) as ProtocolDistributionStoreResult.Persisted
        val duplicateBatch = store.persistBatch(batch) as ProtocolDistributionStoreResult.Persisted

        assertEquals(1, firstVesting.inserted)
        assertEquals(1, duplicateVesting.duplicates)
        assertEquals(1, firstBatch.inserted)
        assertEquals(1, duplicateBatch.duplicates)
        val rows = database.assetObservationDao().observeForAsset("asset:ethereum").first()
        assertEquals(2, rows.size)
        assertTrue(rows.all { it.status == "QUARANTINED" && it.quality == "UNVERIFIED" })
        assertTrue(rows.all { it.payloadSha256?.isNotBlank() == true })
    }

    @Test fun refusesBatchEvidenceWithoutCanonicalTokenDeployment() = runBlocking {
        InitialAssetCatalog(database) { now }.synchronize()
        val batch = VerifiedBatchDistributionCandidate(
            protocolId = "protocol:uniswap", assetId = "asset:ethereum", chainId = 1,
            distributorAddress = "0x090D4613473dEE047c3f2706764f49E0821D256e",
            tokenContract = token, transactionHash = "0x" + "e".repeat(64),
            blockNumber = 20_000_002, blockHash = "0x" + "f".repeat(64),
            blockTimestampEpochMs = now - 1,
            payments = listOf(
                BatchDistributionPayment("0x3333333333333333333333333333333333333333", "1", 1),
                BatchDistributionPayment("0x4444444444444444444444444444444444444444", "2", 2)
            )
        )
        val result = ProtocolDistributionObservationStore(database) { now }.persistBatch(batch)
        assertEquals(
            listOf("DISTRIBUTION_TOKEN_NOT_REGISTERED_FOR_CANONICAL_ASSET"),
            (result as ProtocolDistributionStoreResult.Abstained).blockers
        )
    }
}
