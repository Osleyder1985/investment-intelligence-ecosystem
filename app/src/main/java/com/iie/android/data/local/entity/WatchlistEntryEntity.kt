package com.iie.android.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "watchlist_entries",
    primaryKeys = ["assetId"],
    foreignKeys = [
        ForeignKey(
            entity = AssetEntity::class,
            parentColumns = ["assetId"],
            childColumns = ["assetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["addedAtEpochMs"])]
)
data class WatchlistEntryEntity(
    val assetId: String,
    val addedAtEpochMs: Long,
    val note: String?
)
