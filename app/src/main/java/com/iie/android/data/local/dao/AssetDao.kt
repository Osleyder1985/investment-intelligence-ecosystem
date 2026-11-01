package com.iie.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.iie.android.data.local.entity.AssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {
    @Query(
        """
        SELECT * FROM assets
        WHERE (:query = '' OR name LIKE '%' || :query || '%' COLLATE NOCASE
            OR symbol LIKE '%' || :query || '%' COLLATE NOCASE)
        ORDER BY name COLLATE NOCASE ASC, assetId ASC
        """
    )
    fun observeAssets(query: String): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE assetId = :assetId LIMIT 1")
    fun observeAsset(assetId: String): Flow<AssetEntity?>

    @Query("SELECT * FROM assets WHERE assetId = :assetId LIMIT 1")
    suspend fun getAsset(assetId: String): AssetEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAsset(asset: AssetEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAssets(assets: List<AssetEntity>)

    @Update
    suspend fun updateAsset(asset: AssetEntity)

    @Query("SELECT COUNT(*) FROM assets")
    suspend fun countAssets(): Int
}
