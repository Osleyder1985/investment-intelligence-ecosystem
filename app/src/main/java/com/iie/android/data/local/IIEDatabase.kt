package com.iie.android.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.iie.android.data.local.dao.AssetDao
import com.iie.android.data.local.dao.AssetDeploymentDao
import com.iie.android.data.local.dao.AssetObservationDao
import com.iie.android.data.local.dao.AssetIdentifierDao
import com.iie.android.data.local.dao.DataSourceDao
import com.iie.android.data.local.dao.NetworkDao
import com.iie.android.data.local.dao.WatchlistDao
import com.iie.android.data.local.dao.AssetResearchDossierSnapshotDao
import com.iie.android.data.local.dao.GlobalMarketSnapshotDao
import com.iie.android.data.local.entity.AssetDeploymentEntity
import com.iie.android.data.local.entity.AssetEntity
import com.iie.android.data.local.entity.AssetObservationEntity
import com.iie.android.data.local.entity.AssetIdentifierEntity
import com.iie.android.data.local.entity.DataSourceEntity
import com.iie.android.data.local.entity.NetworkEntity
import com.iie.android.data.local.entity.WatchlistEntryEntity
import com.iie.android.data.local.entity.AssetResearchDossierSnapshotEntity
import com.iie.android.data.local.entity.GlobalMarketSnapshotEntity

@Database(
    entities = [
        AssetEntity::class,
        NetworkEntity::class,
        AssetDeploymentEntity::class,
        DataSourceEntity::class,
        AssetObservationEntity::class,
        AssetIdentifierEntity::class,
        WatchlistEntryEntity::class,
        AssetResearchDossierSnapshotEntity::class,
        GlobalMarketSnapshotEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class IIEDatabase : RoomDatabase() {
    abstract fun assetDao(): AssetDao
    abstract fun networkDao(): NetworkDao
    abstract fun assetDeploymentDao(): AssetDeploymentDao
    abstract fun dataSourceDao(): DataSourceDao
    abstract fun assetObservationDao(): AssetObservationDao
    abstract fun assetIdentifierDao(): AssetIdentifierDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun researchDossierSnapshotDao(): AssetResearchDossierSnapshotDao
    abstract fun globalMarketSnapshotDao(): GlobalMarketSnapshotDao
}
