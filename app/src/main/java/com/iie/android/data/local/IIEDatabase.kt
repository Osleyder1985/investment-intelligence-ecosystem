package com.iie.android.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.iie.android.data.local.dao.AssetDao
import com.iie.android.data.local.dao.AssetDeploymentDao
import com.iie.android.data.local.dao.AssetObservationDao
import com.iie.android.data.local.dao.DataSourceDao
import com.iie.android.data.local.dao.NetworkDao
import com.iie.android.data.local.dao.WatchlistDao
import com.iie.android.data.local.entity.AssetDeploymentEntity
import com.iie.android.data.local.entity.AssetEntity
import com.iie.android.data.local.entity.AssetObservationEntity
import com.iie.android.data.local.entity.DataSourceEntity
import com.iie.android.data.local.entity.NetworkEntity
import com.iie.android.data.local.entity.WatchlistEntryEntity

@Database(
    entities = [
        AssetEntity::class,
        NetworkEntity::class,
        AssetDeploymentEntity::class,
        DataSourceEntity::class,
        AssetObservationEntity::class,
        WatchlistEntryEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class IIEDatabase : RoomDatabase() {
    abstract fun assetDao(): AssetDao
    abstract fun networkDao(): NetworkDao
    abstract fun assetDeploymentDao(): AssetDeploymentDao
    abstract fun dataSourceDao(): DataSourceDao
    abstract fun assetObservationDao(): AssetObservationDao
    abstract fun watchlistDao(): WatchlistDao
}
