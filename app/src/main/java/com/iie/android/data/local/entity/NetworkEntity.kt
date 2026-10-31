package com.iie.android.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "networks",
    indices = [Index(value = ["networkKey"], unique = true)]
)
data class NetworkEntity(
    @PrimaryKey val networkId: String,
    val networkKey: String,
    val name: String,
    val networkFamily: String,
    val chainId: String?,
    val nativeCurrencySymbol: String?
)
