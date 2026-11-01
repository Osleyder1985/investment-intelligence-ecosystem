package com.iie.android.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "global_market_snapshots",
    indices = [Index(value = ["sourceId", "snapshotType", "retrievedAtEpochMs"])]
)
data class GlobalMarketSnapshotEntity(
    @PrimaryKey val snapshotId: String,
    val sourceId: String,
    val snapshotType: String,
    val observedAtEpochMs: Long,
    val retrievedAtEpochMs: Long,
    val quality: String,
    val payloadJson: String,
    val payloadSha256: String
)
