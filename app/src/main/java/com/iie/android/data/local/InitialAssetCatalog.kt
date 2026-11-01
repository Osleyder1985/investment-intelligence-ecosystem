package com.iie.android.data.local

import androidx.room.withTransaction
import com.iie.android.data.local.entity.AssetDeploymentEntity
import com.iie.android.data.local.entity.AssetEntity
import com.iie.android.data.local.entity.AssetIdentifierEntity
import com.iie.android.data.local.entity.DataSourceEntity
import com.iie.android.data.local.entity.NetworkEntity

/**
 * Synchronizes the small built-in asset universe without creating synthetic observations.
 *
 * The catalog contains canonical IIE asset records and explicit CoinPaprika provider IDs.
 * Provider IDs are identifiers, not proof of economic quality or investment merit.
 */
class InitialAssetCatalog(
    private val database: IIEDatabase,
    private val nowEpochMs: () -> Long = { System.currentTimeMillis() }
) {
    suspend fun synchronize() {
        database.withTransaction {
            val synchronizedAt = nowEpochMs()
            require(synchronizedAt > 0L) { "Invalid local synchronization clock" }

            // Remove only the retired local-demo fixture records. Market observations
            // and user-owned watchlist/dossier data are deliberately preserved.
            database.assetObservationDao().deleteBySourceAndType(
                DEMO_SOURCE_ID,
                DEMO_OBSERVATION_TYPE
            )
            database.assetIdentifierDao().deleteBySource(DEMO_SOURCE_ID)
            database.dataSourceDao().deleteSource(DEMO_SOURCE_ID)

            if (database.dataSourceDao().getSource(COINPAPRIKA_SOURCE_ID) == null) {
                database.dataSourceDao().insertSource(
                    DataSourceEntity(
                        sourceId = COINPAPRIKA_SOURCE_ID,
                        sourceKey = COINPAPRIKA_SOURCE_KEY,
                        displayName = "CoinPaprika API",
                        providerType = "MARKET_DATA_PROVIDER",
                        homepageUrl = "https://coinpaprika.com",
                        termsUrl = "https://coinpaprika.com/terms-api",
                        attributionText = "Powered by CoinPaprika",
                        permittedUseStatus = "PERSONAL_USE_ONLY",
                        registeredAtEpochMs = synchronizedAt
                    )
                )
            }

            if (database.dataSourceDao().getSource(COINMARKETCAP_SOURCE_ID) == null) {
                database.dataSourceDao().insertSource(
                    DataSourceEntity(
                        sourceId = COINMARKETCAP_SOURCE_ID,
                        sourceKey = COINMARKETCAP_SOURCE_KEY,
                        displayName = "CoinMarketCap API",
                        providerType = "MARKET_DATA_PROVIDER",
                        homepageUrl = "https://coinmarketcap.com",
                        termsUrl = "https://coinmarketcap.com/terms/",
                        attributionText = "Data provided by CoinMarketCap",
                        permittedUseStatus = "PERSONAL_USE_ONLY",
                        registeredAtEpochMs = synchronizedAt
                    )
                )
            }

            if (database.dataSourceDao().getSource(ALTERNATIVE_ME_SOURCE_ID) == null) {
                database.dataSourceDao().insertSource(
                    DataSourceEntity(
                        sourceId = ALTERNATIVE_ME_SOURCE_ID,
                        sourceKey = ALTERNATIVE_ME_SOURCE_KEY,
                        displayName = "Alternative.me Fear & Greed Index",
                        providerType = "SENTIMENT_DATA_PROVIDER",
                        homepageUrl = "https://alternative.me/crypto/fear-and-greed-index/",
                        termsUrl = "https://alternative.me/crypto/fear-and-greed-index/",
                        attributionText = "Fear & Greed Index provided by Alternative.me",
                        permittedUseStatus = "ATTRIBUTION_REQUIRED",
                        registeredAtEpochMs = synchronizedAt
                    )
                )
            }

            if (database.dataSourceDao().getSource(ROUTESCAN_SOURCE_ID) == null) {
                database.dataSourceDao().insertSource(
                    DataSourceEntity(
                        sourceId = ROUTESCAN_SOURCE_ID,
                        sourceKey = ROUTESCAN_SOURCE_KEY,
                        displayName = "Routescan API",
                        providerType = "ON_CHAIN_DATA_PROVIDER",
                        homepageUrl = "https://routescan.io",
                        termsUrl = "https://docs.routescan.io/plans-and-limits/api-keys-and-pricing",
                        attributionText = "On-chain data indexed by Routescan",
                        permittedUseStatus = "PERSONAL_USE_ONLY",
                        registeredAtEpochMs = synchronizedAt
                    )
                )
            }

            networks.forEach { network ->
                val existing = database.networkDao().getNetwork(network.networkId)
                if (existing == null) {
                    database.networkDao().insertNetwork(network)
                } else if (existing != network) {
                    database.networkDao().updateNetwork(network)
                }
            }

            assets.forEach { catalogAsset ->
                val existing = database.assetDao().getAsset(catalogAsset.assetId)
                if (existing == null) {
                    database.assetDao().insertAsset(
                        catalogAsset.copy(
                            createdAtEpochMs = synchronizedAt,
                            updatedAtEpochMs = synchronizedAt
                        )
                    )
                } else {
                    database.assetDao().updateAsset(
                        existing.copy(
                            canonicalKey = catalogAsset.canonicalKey,
                            name = catalogAsset.name,
                            symbol = catalogAsset.symbol,
                            assetType = catalogAsset.assetType,
                            identityStatus = catalogAsset.identityStatus,
                            updatedAtEpochMs = synchronizedAt
                        )
                    )
                }
            }

            deployments.forEach { catalogDeployment ->
                val existing = database.assetDeploymentDao()
                    .getDeployment(catalogDeployment.deploymentId)
                if (existing == null) {
                    database.assetDeploymentDao().insertDeployment(catalogDeployment)
                } else if (existing != catalogDeployment) {
                    database.assetDeploymentDao().updateDeployment(catalogDeployment)
                }
            }

            providerIdentifiers.forEach { identifier ->
                val existing = database.assetIdentifierDao().findIdentifier(
                    identifier.sourceId,
                    identifier.namespace,
                    identifier.identifierValue
                )
                if (existing == null) {
                    database.assetIdentifierDao().insertIdentifier(identifier)
                }
            }
        }
    }

    private companion object {
        const val DEMO_SOURCE_ID = "source:iie-demo-fixture"
        const val DEMO_OBSERVATION_TYPE = "IDENTITY_FIXTURE"
        const val COINPAPRIKA_SOURCE_ID = "source:coinpaprika"
        const val COINPAPRIKA_SOURCE_KEY = "coinpaprika-api-v1"
        const val COINMARKETCAP_SOURCE_ID = "source:coinmarketcap"
        const val COINMARKETCAP_SOURCE_KEY = "coinmarketcap-api-v3"
        const val ALTERNATIVE_ME_SOURCE_ID = "source:alternative-me"
        const val ALTERNATIVE_ME_SOURCE_KEY = "alternative-me-fng-v1"
        const val ROUTESCAN_SOURCE_ID = "source:routescan"
        const val ROUTESCAN_SOURCE_KEY = "routescan-evm-api-v1"

        val assets = listOf(
            AssetEntity("asset:bitcoin", "crypto:bitcoin", "Bitcoin", "BTC", "CRYPTO_ASSET", "RESOLVED", 0L, 0L),
            AssetEntity("asset:ethereum", "crypto:ethereum", "Ethereum", "ETH", "CRYPTO_ASSET", "RESOLVED", 0L, 0L),
            AssetEntity("asset:solana", "crypto:solana", "Solana", "SOL", "CRYPTO_ASSET", "RESOLVED", 0L, 0L),
            AssetEntity("asset:usd-coin", "crypto:usd-coin", "USD Coin", "USDC", "CRYPTO_ASSET", "RESOLVED_WITH_LIMITATIONS", 0L, 0L),
            AssetEntity("asset:tether", "crypto:tether", "Tether", "USDT", "CRYPTO_ASSET", "RESOLVED_WITH_LIMITATIONS", 0L, 0L)
        )

        val networks = listOf(
            NetworkEntity("network:bitcoin-mainnet", "network:bitcoin", "Bitcoin", "UTXO", null, "BTC"),
            NetworkEntity("network:ethereum-mainnet", "network:ethereum", "Ethereum", "ACCOUNT", "1", "ETH"),
            NetworkEntity("network:solana-mainnet", "network:solana", "Solana", "ACCOUNT", "mainnet-beta", "SOL")
        )

        val deployments = listOf(
            AssetDeploymentEntity("deployment:bitcoin-native", "deployment:bitcoin:native", "asset:bitcoin", "network:bitcoin-mainnet", "NATIVE", "native", null, 8, null, null),
            AssetDeploymentEntity("deployment:ethereum-native", "deployment:ethereum:native", "asset:ethereum", "network:ethereum-mainnet", "NATIVE", "native", null, 18, null, null),
            AssetDeploymentEntity("deployment:solana-native", "deployment:solana:native", "asset:solana", "network:solana-mainnet", "NATIVE", "native", null, 9, null, null),
            AssetDeploymentEntity("deployment:usdc-ethereum-erc20", "deployment:usdc:eip155:1:0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48", "asset:usd-coin", "network:ethereum-mainnet", "ERC20", "eip155", "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48", 6, null, null),
            AssetDeploymentEntity("deployment:usdt-ethereum-erc20", "deployment:usdt:eip155:1:0xdac17f958d2ee523a2206206994597c13d831ec7", "asset:tether", "network:ethereum-mainnet", "ERC20", "eip155", "0xdac17f958d2ee523a2206206994597c13d831ec7", 6, null, null)
        )

        val providerIdentifiers = listOf(
            AssetIdentifierEntity("identifier:coinpaprika:btc", "coinpaprika:btc-bitcoin", "asset:bitcoin", COINPAPRIKA_SOURCE_ID, "coinpaprika", "btc-bitcoin", null, null, "PROVIDER_ID_CONFIRMED"),
            AssetIdentifierEntity("identifier:coinpaprika:eth", "coinpaprika:eth-ethereum", "asset:ethereum", COINPAPRIKA_SOURCE_ID, "coinpaprika", "eth-ethereum", null, null, "PROVIDER_ID_CONFIRMED"),
            AssetIdentifierEntity("identifier:coinpaprika:sol", "coinpaprika:sol-solana", "asset:solana", COINPAPRIKA_SOURCE_ID, "coinpaprika", "sol-solana", null, null, "PROVIDER_ID_CONFIRMED"),
            AssetIdentifierEntity("identifier:coinpaprika:usdc", "coinpaprika:usdc-usd-coin", "asset:usd-coin", COINPAPRIKA_SOURCE_ID, "coinpaprika", "usdc-usd-coin", null, null, "PROVIDER_ID_CONFIRMED"),
            AssetIdentifierEntity("identifier:coinpaprika:usdt", "coinpaprika:usdt-tether", "asset:tether", COINPAPRIKA_SOURCE_ID, "coinpaprika", "usdt-tether", null, null, "PROVIDER_ID_CONFIRMED"),
            AssetIdentifierEntity("identifier:coinmarketcap:btc", "coinmarketcap:1", "asset:bitcoin", COINMARKETCAP_SOURCE_ID, "coinmarketcap", "1", null, null, "PROVIDER_ID_CONFIRMED"),
            AssetIdentifierEntity("identifier:coinmarketcap:eth", "coinmarketcap:1027", "asset:ethereum", COINMARKETCAP_SOURCE_ID, "coinmarketcap", "1027", null, null, "PROVIDER_ID_CONFIRMED"),
            AssetIdentifierEntity("identifier:coinmarketcap:sol", "coinmarketcap:5426", "asset:solana", COINMARKETCAP_SOURCE_ID, "coinmarketcap", "5426", null, null, "PROVIDER_ID_CONFIRMED"),
            AssetIdentifierEntity("identifier:coinmarketcap:usdc", "coinmarketcap:3408", "asset:usd-coin", COINMARKETCAP_SOURCE_ID, "coinmarketcap", "3408", null, null, "PROVIDER_ID_CONFIRMED"),
            AssetIdentifierEntity("identifier:coinmarketcap:usdt", "coinmarketcap:825", "asset:tether", COINMARKETCAP_SOURCE_ID, "coinmarketcap", "825", null, null, "PROVIDER_ID_CONFIRMED")
        )
    }
}
