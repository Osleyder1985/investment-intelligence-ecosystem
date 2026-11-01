package com.iie.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iie.android.data.local.entity.AssetObservationEntity
import com.iie.android.data.local.projection.AssetObservationProvenanceRow
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetObservationDao {
    @Query(
        """
        SELECT * FROM asset_observations
        WHERE assetId = :assetId
        ORDER BY retrievedAtEpochMs DESC, observationId DESC
        """
    )
    fun observeForAsset(assetId: String): Flow<List<AssetObservationEntity>>

    @Query(
        """
        SELECT
            o.observationId AS observationId,
            s.displayName AS sourceDisplayName,
            s.providerType AS sourceProviderType,
            s.permittedUseStatus AS sourcePermittedUseStatus,
            o.observationType AS observationType,
            o.observedAtEpochMs AS observedAtEpochMs,
            o.retrievedAtEpochMs AS retrievedAtEpochMs,
            o.status AS status,
            o.quality AS quality
        FROM asset_observations AS o
        INNER JOIN data_sources AS s ON s.sourceId = o.sourceId
        WHERE o.assetId = :assetId
        ORDER BY o.retrievedAtEpochMs DESC, o.observationId DESC
        """
    )
    fun observeProvenanceForAsset(assetId: String): Flow<List<AssetObservationProvenanceRow>>

    @Query(
        """
        SELECT * FROM asset_observations
        WHERE assetId = :assetId AND observationType = :observationType
        ORDER BY retrievedAtEpochMs DESC, observationId DESC
        LIMIT 1
        """
    )
    suspend fun getLatest(assetId: String, observationType: String): AssetObservationEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertObservation(observation: AssetObservationEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertObservations(observations: List<AssetObservationEntity>)
}
