package com.iie.android.domain.repository

import com.iie.android.domain.model.Asset
import kotlinx.coroutines.flow.Flow

/**
 * Domain-facing contract for canonical asset and local watchlist access.
 * Implementations belong to the data layer.
 */
interface AssetRepository {
    fun observeAssets(query: String = ""): Flow<List<Asset>>
    fun observeAsset(assetId: String): Flow<Asset?>
    suspend fun addToWatchlist(assetId: String, note: String? = null)
    suspend fun removeFromWatchlist(assetId: String)
    fun observeWatchlist(): Flow<List<String>>
}
