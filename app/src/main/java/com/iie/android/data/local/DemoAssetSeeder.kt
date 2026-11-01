package com.iie.android.data.local

import androidx.room.withTransaction
import com.iie.android.data.local.entity.AssetDeploymentEntity
import com.iie.android.data.local.entity.AssetEntity
import com.iie.android.data.local.entity.AssetIdentifierEntity
import com.iie.android.data.local.entity.AssetObservationEntity
import com.iie.android.data.local.entity.DataSourceEntity
import com.iie.android.data.local.entity.NetworkEntity

/**
 * Inserts a small, explicitly non-production catalogue for the first-run experience.
 * Existing records are never replaced; all missing fixture rows are inserted atomically.
 */
class DemoAssetSeeder(
    private val database: IIEDatabase,
    private val nowEpochMs: () -> Long = { System.currentTimeMillis() }
) {
    suspend fun seed() {
        database.withTransaction {
            val seededAt = nowEpochMs()
            if (database.dataSourceDao().getSource(SOURCE_ID) == null) {
                database.dataSourceDao().insertSource(
                    DataSourceEntity(
                        sourceId = SOURCE_ID,
                        sourceKey = SOURCE_KEY,
                        displayName = "IIE local demonstration fixture",
                        providerType = "LOCAL_FIXTURE",
                        homepageUrl = null,
                        termsUrl = null,
                        attributionText = "Synthetic/local demonstration records; not market data.",
                        permittedUseStatus = "INTERNAL_DEMO_ONLY",
                        registeredAtEpochMs = seededAt
                    )
                )
            }

            networks.forEach { network ->
                if (database.networkDao().getNetwork(network.networkId) == null) {
                    database.networkDao().insertNetwork(network)
                }
            }
            assets.forEach { asset ->
                if (database.assetDao().getAsset(asset.assetId) == null) {
                    database.assetDao().insertAsset(
                        asset.copy(createdAtEpochMs = seededAt, updatedAtEpochMs = seededAt)
                    )
                }
            }
            deployments.forEach { deployment ->
                if (database.assetDeploymentDao().getDeployment(deployment.deploymentId) == null) {
                    database.assetDeploymentDao().insertDeployment(deployment)
                }
            }
            identifiers.forEach { identifier ->
                if (database.assetIdentifierDao().findIdentifier(
                        identifier.sourceId, identifier.namespace, identifier.identifierValue
                    ) == null
                ) {
                    database.assetIdentifierDao().insertIdentifier(identifier)
                }
            }
            // Record provenance for every demo asset. These are explicitly fixture
            // observations, not provider observations or market-price measurements.
            assets.forEach { asset ->
                if (database.assetObservationDao().getLatest(asset.assetId, OBSERVATION_TYPE) == null) {
                    database.assetObservationDao().insertObservation(
                        AssetObservationEntity(
                            observationId = "observation:demo:${asset.assetId}",
                            assetId = asset.assetId,
                            deploymentId = null,
                            sourceId = SOURCE_ID,
                            observationType = OBSERVATION_TYPE,
                            observedAtEpochMs = seededAt,
                            retrievedAtEpochMs = seededAt,
                            status = "DEMO",
                            quality = "UNVERIFIED",
                            valueSchemaVersion = 1,
                            valueJson = """{"recordType":"synthetic_demo_fixture","marketData":false}""",
                            payloadSha256 = null,
                            licenseReference = null
                        )
                    )
                }
            }
        }
    }

    private companion object {
        const val SOURCE_ID = "source:iie-demo-fixture"
        const val SOURCE_KEY = "iie-local-demo-fixture-v1"
        const val OBSERVATION_TYPE = "IDENTITY_FIXTURE"

        val assets = listOf(
            AssetEntity("asset:bitcoin", "demo:bitcoin:native", "Bitcoin", "BTC", "CRYPTO_ASSET", "DEMO_UNVERIFIED", 0L, 0L),
            AssetEntity("asset:ethereum", "demo:ethereum:native", "Ethereum", "ETH", "CRYPTO_ASSET", "DEMO_UNVERIFIED", 0L, 0L),
            AssetEntity("asset:solana", "demo:solana:native", "Solana", "SOL", "CRYPTO_ASSET", "DEMO_UNVERIFIED", 0L, 0L),
            AssetEntity("asset:usd-coin", "demo:usd-coin:unresolved", "USD Coin", "USDC", "CRYPTO_ASSET", "DEMO_UNVERIFIED", 0L, 0L)
        )

        val networks = listOf(
            NetworkEntity("network:bitcoin-mainnet", "demo:bitcoin-mainnet", "Bitcoin mainnet (demo)", "UTXO", null, "BTC"),
            NetworkEntity("network:ethereum-mainnet", "demo:ethereum-mainnet", "Ethereum mainnet (demo)", "ACCOUNT", "1", "ETH"),
            NetworkEntity("network:solana-mainnet", "demo:solana-mainnet", "Solana mainnet-beta (demo)", "ACCOUNT", "mainnet-beta", "SOL")
        )

        val deployments = listOf(
            AssetDeploymentEntity("deployment:bitcoin-native", "demo:native:bitcoin", "asset:bitcoin", "network:bitcoin-mainnet", "NATIVE", "native", null, 8, null, null),
            AssetDeploymentEntity("deployment:ethereum-native", "demo:native:ethereum", "asset:ethereum", "network:ethereum-mainnet", "NATIVE", "native", null, 18, null, null),
            AssetDeploymentEntity("deployment:solana-native", "demo:native:solana", "asset:solana", "network:solana-mainnet", "NATIVE", "native", null, 9, null, null)
        )

        val identifiers = listOf(
            AssetIdentifierEntity("identifier:bitcoin", "demo:identifier:bitcoin", "asset:bitcoin", SOURCE_ID, "iie-demo-fixture", "bitcoin", null, null, "DEMO_ONLY"),
            AssetIdentifierEntity("identifier:ethereum", "demo:identifier:ethereum", "asset:ethereum", SOURCE_ID, "iie-demo-fixture", "ethereum", null, null, "DEMO_ONLY"),
            AssetIdentifierEntity("identifier:solana", "demo:identifier:solana", "asset:solana", SOURCE_ID, "iie-demo-fixture", "solana", null, null, "DEMO_ONLY"),
            AssetIdentifierEntity("identifier:usd-coin", "demo:identifier:usd-coin", "asset:usd-coin", SOURCE_ID, "iie-demo-fixture", "usd-coin", null, null, "DEMO_ONLY")
        )
    }
}
