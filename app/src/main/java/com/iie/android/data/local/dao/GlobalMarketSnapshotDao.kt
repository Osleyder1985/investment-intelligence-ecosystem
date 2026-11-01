package com.iie.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iie.android.data.local.entity.GlobalMarketSnapshotEntity

@Dao
interface GlobalMarketSnapshotDao {
    @Query(
        """
        SELECT * FROM global_market_snapshots
        WHERE sourceId = :sourceId AND snapshotType = :snapshotType
        ORDER BY retrievedAtEpochMs DESC, snapshotId DESC
        LIMIT 1
        """
    )
    suspend fun getLatest(sourceId: String, snapshotType: String): GlobalMarketSnapshotEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(snapshot: GlobalMarketSnapshotEntity)
}
