package com.iie.android.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "assets",
    indices = [
        Index(value = ["canonicalKey"], unique = true),
        Index(value = ["symbol"]),
        Index(value = ["name"])
    ]
)
data class AssetEntity(
    @PrimaryKey val assetId: String,
    val canonicalKey: String,
    val name: String,
    val symbol: String,
    val assetType: String,
    val identityStatus: String,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)
