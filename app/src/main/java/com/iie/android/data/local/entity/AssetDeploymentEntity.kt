package com.iie.android.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "asset_deployments",
    foreignKeys = [
        ForeignKey(
            entity = AssetEntity::class,
            parentColumns = ["assetId"],
            childColumns = ["assetId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = NetworkEntity::class,
            parentColumns = ["networkId"],
            childColumns = ["networkId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["deploymentKey"], unique = true),
        Index(value = ["assetId"]),
        Index(value = ["networkId"]),
        Index(value = ["networkId", "namespace", "reference"])
    ]
)
data class AssetDeploymentEntity(
    @PrimaryKey val deploymentId: String,
    val deploymentKey: String,
    val assetId: String,
    val networkId: String,
    val representationKind: String,
    val namespace: String,
    val reference: String?,
    val decimals: Int?,
    val validFromEpochMs: Long?,
    val validUntilEpochMs: Long?
)
