package com.iie.android.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "data_sources",
    indices = [Index(value = ["sourceKey"], unique = true)]
)
data class DataSourceEntity(
    @PrimaryKey val sourceId: String,
    val sourceKey: String,
    val displayName: String,
    val providerType: String,
    val homepageUrl: String?,
    val termsUrl: String?,
    val attributionText: String?,
    val permittedUseStatus: String,
    val registeredAtEpochMs: Long
)
