package com.iie.android.data.repository

import com.iie.android.domain.model.Asset
import kotlinx.coroutines.flow.Flow

interface AssetRepository {
    fun observeAssets(query: String = ""): Flow<List<Asset>>
    fun observeAsset(assetId: String): Flow<Asset?>
    suspend fun addToWatchlist(assetId: String, note: String? = null)
    suspend fun removeFromWatchlist(assetId: String)
    fun observeWatchlist(): Flow<List<String>>
}
