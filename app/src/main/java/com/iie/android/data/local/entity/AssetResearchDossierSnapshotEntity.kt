package com.iie.android.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "asset_research_dossier_snapshots",
    foreignKeys = [
        ForeignKey(
            entity = AssetEntity::class,
            parentColumns = ["assetId"],
            childColumns = ["assetId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["assetId", "generatedAtEpochMs"])]
)
data class AssetResearchDossierSnapshotEntity(
    @PrimaryKey val snapshotId: String,
    val assetId: String,
    val generatedAtEpochMs: Long,
    val schemaVersion: Int,
    val evidenceStatus: String,
    val payloadJson: String,
    val payloadSha256: String
)
