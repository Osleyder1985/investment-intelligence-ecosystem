package com.iie.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iie.android.data.local.entity.AssetResearchDossierSnapshotEntity

@Dao
interface AssetResearchDossierSnapshotDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(snapshot: AssetResearchDossierSnapshotEntity)

    @Query("""
        SELECT * FROM asset_research_dossier_snapshots
        WHERE assetId = :assetId
        ORDER BY generatedAtEpochMs DESC, snapshotId DESC
        LIMIT 1
    """)
    suspend fun getLatestForAsset(assetId: String): AssetResearchDossierSnapshotEntity?
}
