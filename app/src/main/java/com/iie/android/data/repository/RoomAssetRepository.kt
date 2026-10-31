package com.iie.android.data.repository

import com.iie.android.data.local.IIEDatabase
import com.iie.android.data.local.entity.WatchlistEntryEntity
import com.iie.android.domain.model.Asset
import com.iie.android.domain.model.AssetDeploymentSummary
import com.iie.android.domain.repository.AssetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomAssetRepository(
    private val database: IIEDatabase,
    private val nowEpochMs: () -> Long = { System.currentTimeMillis() }
) : AssetRepository {

    override fun observeAssets(query: String): Flow<List<Asset>> =
        database.assetDao().observeAssets(query.trim()).map { entities ->
            entities.map { entity ->
                Asset(
                    assetId = entity.assetId,
                    canonicalKey = entity.canonicalKey,
                    name = entity.name,
                    symbol = entity.symbol,
                    assetType = entity.assetType,
                    identityStatus = entity.identityStatus,
                    createdAtEpochMs = entity.createdAtEpochMs,
                    updatedAtEpochMs = entity.updatedAtEpochMs
                )
            }
        }

    override fun observeAsset(assetId: String): Flow<Asset?> =
        database.assetDao().observeAsset(assetId).map { entity ->
            entity?.let {
                Asset(
                    assetId = it.assetId,
                    canonicalKey = it.canonicalKey,
                    name = it.name,
                    symbol = it.symbol,
                    assetType = it.assetType,
                    identityStatus = it.identityStatus,
                    createdAtEpochMs = it.createdAtEpochMs,
                    updatedAtEpochMs = it.updatedAtEpochMs
                )
            }
        }

    override fun observeDeployments(assetId: String): Flow<List<AssetDeploymentSummary>> =
        database.assetDeploymentDao().observeSummariesForAsset(assetId).map { rows ->
            rows.map { row ->
                AssetDeploymentSummary(
                    deploymentId = row.deploymentId,
                    networkName = row.networkName,
                    representationKind = row.representationKind,
                    namespace = row.namespace,
                    reference = row.reference,
                    decimals = row.decimals
                )
            }
        }

    override suspend fun addToWatchlist(assetId: String, note: String?) {
        require(database.assetDao().getAsset(assetId) != null) {
            "Cannot watchlist an asset that does not exist: $assetId"
        }
        database.watchlistDao().add(
            WatchlistEntryEntity(
                assetId = assetId,
                addedAtEpochMs = nowEpochMs(),
                note = note
            )
        )
    }

    override suspend fun removeFromWatchlist(assetId: String) {
        database.watchlistDao().remove(assetId)
    }

    override fun observeWatchlist(): Flow<List<String>> =
        database.watchlistDao().observeEntries().map { entries ->
            entries.map(WatchlistEntryEntity::assetId)
        }
}
