package com.iie.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iie.android.data.local.entity.NetworkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkDao {
    @Query("SELECT * FROM networks ORDER BY name COLLATE NOCASE ASC")
    fun observeNetworks(): Flow<List<NetworkEntity>>

    @Query("SELECT * FROM networks WHERE networkId = :networkId LIMIT 1")
    suspend fun getNetwork(networkId: String): NetworkEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNetwork(network: NetworkEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNetworks(networks: List<NetworkEntity>)
}
