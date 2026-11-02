package com.iie.android.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.iie.android.data.local.entity.AssetDeploymentEntity
import com.iie.android.data.local.entity.AssetObservationEntity
import com.iie.android.data.local.entity.DataSourceEntity
import com.iie.android.data.local.entity.NetworkEntity
import com.iie.android.data.repository.DistributionReconciliationPersistenceResult
import com.iie.android.data.repository.DistributionReconciliationRepository
import com.iie.android.domain.valuation.DistributionMovementKind
import com.iie.android.domain.valuation.DistributionReconciliationInput
import com.iie.android.domain.valuation.ObservedDistributionMovement
import com.iie.android.domain.valuation.SellThroughAssumption
import java.security.MessageDigest
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DistributionReconciliationRepositoryTest {
    private lateinit var database: IIEDatabase
    private val dbName = "distribution-reconciliation-${UUID.randomUUID()}.db"
    private val now = 1_793_000_000_000L
    private val token = "0x1f9840a85d5aF5bf1D1762F925BDADdC4201F984"
    private val beneficiary = "0x2222222222222222222222222222222222222222"
    private val transactionHash = "0x" + "a".repeat(64)
    private val evidenceId = "evidence:claim:001"
    private val sourceId = "reviewed-source:uni"

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

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    private suspend fun registerCanonicalDeployment() {
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
        database.dataSourceDao().insertSource(
            DataSourceEntity(
                sourceId = sourceId,
                sourceKey = sourceId,
                displayName = "Reviewed UNI evidence source",
                providerType = "ONCHAIN_PROTOCOL_DISTRIBUTION",
                homepageUrl = "https://uniswap.org/",
                termsUrl = null,
                attributionText = "Test fixture",
                permittedUseStatus = "APPROVED",
                registeredAtEpochMs = now - 10_000
            )
        )
    }

    private fun payload(amount: String = "400", beneficiaryAddress: String = beneficiary) =
        """{"schema":"test.reviewed-claim.v1","transactionHash":"$transactionHash","blockTimestampEpochMs":$now,"beneficiaryAddress":"$beneficiaryAddress","rawAmount":"$amount"}"""

    private suspend fun insertEvidence(
        status: String = "ACCEPTED",
        quality: String = "VERIFIED",
        sourceStatus: String = "APPROVED",
        amount: String = "400",
        beneficiaryAddress: String = beneficiary,
        corruptHash: Boolean = false
    ) {
        if (sourceStatus != "APPROVED") {
            database.dataSourceDao().deleteSource(sourceId)
            database.dataSourceDao().insertSource(
                DataSourceEntity(
                    sourceId = sourceId, sourceKey = sourceId,
                    displayName = "Reviewed UNI evidence source",
                    providerType = "ONCHAIN_PROTOCOL_DISTRIBUTION",
                    homepageUrl = "https://uniswap.org/", termsUrl = null,
                    attributionText = "Test fixture",
                    permittedUseStatus = sourceStatus, registeredAtEpochMs = now - 10_000
                )
            )
        }
        val value = payload(amount, beneficiaryAddress)
        database.assetObservationDao().insertObservation(
            AssetObservationEntity(
                observationId = evidenceId,
                assetId = "asset:ethereum",
                deploymentId = "deployment:uni",
                sourceId = sourceId,
                observationType = "ONCHAIN_DISTRIBUTION_CLAIM_PAYMENT",
                observedAtEpochMs = now,
                retrievedAtEpochMs = now,
                status = status,
                quality = quality,
                valueSchemaVersion = 1,
                valueJson = value,
                payloadSha256 = if (corruptHash) "0".repeat(64) else sha256(value),
                licenseReference = null
            )
        )
    }

    private fun input(amount: String = "400", beneficiaryAddress: String = beneficiary) =
        DistributionReconciliationInput(
            windowStartEpochMs = now - 5_000,
            windowEndEpochMs = now,
            circulatingSupplyRaw = "10000",
            schedules = emptyList(),
            movements = listOf(
                ObservedDistributionMovement(
                    movementId = "movement:claim:001",
                    beneficiary = beneficiaryAddress,
                    observedAtEpochMs = now,
                    rawAmount = amount,
                    kind = DistributionMovementKind.CLAIMED_AND_TRANSFERRED,
                    evidenceId = evidenceId,
                    transactionHash = transactionHash,
                    verified = true
                )
            ),
            sellThrough = SellThroughAssumption(
                lowBasisPoints = 1_000,
                highBasisPoints = 5_000,
                rationale = "Illustrative scenario assumption",
                evidenceId = "assumption:sell-through:v1"
            )
        )

    @Test fun persistsReportOnlyFromAcceptedVerifiedApprovedEvidenceAndIsIdempotent() = runBlocking {
        registerCanonicalDeployment()
        insertEvidence()
        val repository = DistributionReconciliationRepository(database) { now }

        val first = repository.reconcileAndPersist("asset:ethereum", input())
        val second = repository.reconcileAndPersist("asset:ethereum", input())

        val firstResult = first as DistributionReconciliationPersistenceResult.Persisted
        val secondResult = second as DistributionReconciliationPersistenceResult.Persisted
        assertTrue(firstResult.inserted)
        assertFalse(secondResult.inserted)
        assertEquals(firstResult.observationId, secondResult.observationId)
        assertEquals(1, firstResult.evidenceCount)
        val stored = database.assetObservationDao().getObservationById(firstResult.observationId)!!
        assertEquals("DISTRIBUTION_RECONCILIATION_REPORT", stored.observationType)
        assertEquals("ACCEPTED", stored.status)
        assertEquals("DERIVED", stored.quality)
        assertEquals(sha256(stored.valueJson), stored.payloadSha256)
        assertTrue(stored.valueJson.contains("\"estimatedSellPressureLowRaw\":\"40\""))
    }

    @Test fun rejectsQuarantinedEvidenceWithoutPersistingReport() = runBlocking {
        registerCanonicalDeployment()
        insertEvidence(status = "QUARANTINED", quality = "UNVERIFIED")
        val result = DistributionReconciliationRepository(database) { now }
            .reconcileAndPersist("asset:ethereum", input())
        assertTrue((result as DistributionReconciliationPersistenceResult.Abstained)
            .blockers.any { it.startsWith("EVIDENCE_NOT_ACCEPTED_AND_VERIFIED") })
        assertEquals(null, database.assetObservationDao().getLatest(
            "asset:ethereum", "DISTRIBUTION_RECONCILIATION_REPORT"
        ))
    }

    @Test fun rejectsEvidenceFromUnapprovedSource() = runBlocking {
        registerCanonicalDeployment()
        insertEvidence(sourceStatus = "REVIEW_REQUIRED")
        val result = DistributionReconciliationRepository(database) { now }
            .reconcileAndPersist("asset:ethereum", input())
        assertTrue((result as DistributionReconciliationPersistenceResult.Abstained)
            .blockers.any { it.startsWith("EVIDENCE_SOURCE_NOT_APPROVED") })
    }

    @Test fun rejectsCorruptEvidencePayloadHash() = runBlocking {
        registerCanonicalDeployment()
        insertEvidence(corruptHash = true)
        val result = DistributionReconciliationRepository(database) { now }
            .reconcileAndPersist("asset:ethereum", input())
        assertTrue((result as DistributionReconciliationPersistenceResult.Abstained)
            .blockers.any { it.startsWith("EVIDENCE_PAYLOAD_HASH_MISMATCH") })
    }

    @Test fun rejectsMovementAmountOrBeneficiaryThatDiffersFromEvidence() = runBlocking {
        registerCanonicalDeployment()
        insertEvidence()
        val repository = DistributionReconciliationRepository(database) { now }

        val wrongAmount = repository.reconcileAndPersist("asset:ethereum", input(amount = "401"))
        assertTrue((wrongAmount as DistributionReconciliationPersistenceResult.Abstained)
            .blockers.contains("MOVEMENT_AMOUNT_MISMATCH:$evidenceId"))

        val wrongBeneficiary = repository.reconcileAndPersist(
            "asset:ethereum",
            input(beneficiaryAddress = "0x3333333333333333333333333333333333333333")
        )
        assertTrue((wrongBeneficiary as DistributionReconciliationPersistenceResult.Abstained)
            .blockers.contains("MOVEMENT_BENEFICIARY_MISMATCH:$evidenceId"))
    }
}
