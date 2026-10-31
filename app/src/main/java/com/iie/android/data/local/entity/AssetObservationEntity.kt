package com.iie.android.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "asset_observations",
    foreignKeys = [
        ForeignKey(
            entity = AssetEntity::class,
            parentColumns = ["assetId"],
            childColumns = ["assetId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = AssetDeploymentEntity::class,
            parentColumns = ["deploymentId", "assetId"],
            childColumns = ["deploymentId", "assetId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = DataSourceEntity::class,
            parentColumns = ["sourceId"],
            childColumns = ["sourceId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["assetId", "observationType", "retrievedAtEpochMs"]),
        Index(value = ["deploymentId"]),
        Index(value = ["sourceId"]),
        Index(value = ["retrievedAtEpochMs"])
    ]
)
data class AssetObservationEntity(
    @PrimaryKey val observationId: String,
    val assetId: String,
    val deploymentId: String?,
    val sourceId: String,
    val observationType: String,
    val observedAtEpochMs: Long?,
    val retrievedAtEpochMs: Long,
    val status: String,
    val quality: String,
    val valueSchemaVersion: Int,
    val valueJson: String,
    val payloadSha256: String?,
    val licenseReference: String?
)
