package com.iie.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iie.android.data.local.entity.WatchlistEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist_entries ORDER BY addedAtEpochMs DESC, assetId ASC")
    fun observeEntries(): Flow<List<WatchlistEntryEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_entries WHERE assetId = :assetId)")
    fun observeContains(assetId: String): Flow<Boolean>

    @Query("SELECT * FROM watchlist_entries WHERE assetId = :assetId LIMIT 1")
    suspend fun getEntry(assetId: String): WatchlistEntryEntity?

    /** Idempotent add: a repeated request does not overwrite the original timestamp or note. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(entry: WatchlistEntryEntity)

    @Query("DELETE FROM watchlist_entries WHERE assetId = :assetId")
    suspend fun remove(assetId: String)

    @Query("UPDATE watchlist_entries SET note = :note WHERE assetId = :assetId")
    suspend fun updateNote(assetId: String, note: String?)
}
