package com.iie.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iie.android.data.local.entity.AssetIdentifierEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetIdentifierDao {
    @Query(
        """
        SELECT * FROM asset_identifiers
        WHERE assetId = :assetId
        ORDER BY sourceId, namespace, identifierValue
        """
    )
    fun observeIdentifiersForAsset(assetId: String): Flow<List<AssetIdentifierEntity>>

    @Query(
        """
        SELECT * FROM asset_identifiers
        WHERE sourceId = :sourceId
          AND namespace = :namespace
          AND identifierValue = :identifierValue
        LIMIT 1
        """
    )
    suspend fun findIdentifier(
        sourceId: String,
        namespace: String,
        identifierValue: String
    ): AssetIdentifierEntity?

    @Query("""
        SELECT * FROM asset_identifiers
        WHERE assetId = :assetId AND sourceId = :sourceId AND namespace = :namespace
        LIMIT 1
    """)
    suspend fun findForAsset(assetId: String, sourceId: String, namespace: String): AssetIdentifierEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertIdentifier(identifier: AssetIdentifierEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertIdentifiers(identifiers: List<AssetIdentifierEntity>)
}
