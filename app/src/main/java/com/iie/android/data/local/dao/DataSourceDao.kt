package com.iie.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iie.android.data.local.entity.DataSourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DataSourceDao {
    @Query("SELECT * FROM data_sources ORDER BY displayName COLLATE NOCASE ASC")
    fun observeSources(): Flow<List<DataSourceEntity>>

    @Query("SELECT * FROM data_sources WHERE sourceId = :sourceId LIMIT 1")
    suspend fun getSource(sourceId: String): DataSourceEntity?

    @Query("DELETE FROM data_sources WHERE sourceId = :sourceId")
    suspend fun deleteSource(sourceId: String)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSource(source: DataSourceEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSources(sources: List<DataSourceEntity>)
}
