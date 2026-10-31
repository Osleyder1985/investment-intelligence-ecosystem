package com.iie.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iie.android.data.local.entity.AssetObservationEntity
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
