package com.iie.android.domain.repository

import com.iie.android.domain.model.Asset
import com.iie.android.domain.model.AssetDeploymentSummary
import com.iie.android.domain.model.AssetObservationProvenance
import kotlinx.coroutines.flow.Flow

/**
 * Domain-facing contract for canonical asset, provenance, and local watchlist access.
 * Implementations belong to the data layer.
 */
interface AssetRepository {
    fun observeAssets(query: String = ""): Flow<List<Asset>>
    fun observeAsset(assetId: String): Flow<Asset?>
    fun observeDeployments(assetId: String): Flow<List<AssetDeploymentSummary>>
    fun observeProvenance(assetId: String): Flow<List<AssetObservationProvenance>>
    suspend fun addToWatchlist(assetId: String, note: String? = null)
    suspend fun toggleWatchlist(assetId: String): Boolean
    suspend fun removeFromWatchlist(assetId: String)
    fun observeWatchlist(): Flow<List<String>>
}
