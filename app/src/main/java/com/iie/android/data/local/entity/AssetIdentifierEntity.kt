package com.iie.android.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Provider- or namespace-specific identifier mapped to an IIE canonical asset.
 * Symbols and names must not be used as globally unique identifiers.
 */
@Entity(
    tableName = "asset_identifiers",
    foreignKeys = [
        ForeignKey(
            entity = AssetEntity::class,
            parentColumns = ["assetId"],
            childColumns = ["assetId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = DataSourceEntity::class,
            parentColumns = ["sourceId"],
            childColumns = ["sourceId"],
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
        Index(value = ["identifierKey"], unique = true),
        Index(value = ["assetId"]),
        Index(value = ["sourceId"]),
        Index(value = ["networkId"])
    ]
)
data class AssetIdentifierEntity(
    @PrimaryKey val identifierId: String,
    val identifierKey: String,
    val assetId: String,
    val sourceId: String,
    val namespace: String,
    val identifierValue: String,
    val networkId: String?,
    val observedAtEpochMs: Long?,
    val verificationStatus: String
)
