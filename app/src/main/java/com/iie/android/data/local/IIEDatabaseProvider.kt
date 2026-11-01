package com.iie.android.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Process-wide database provider. The database is stored in app-private storage.
 * Destructive migrations are intentionally not enabled.
 */
object IIEDatabaseProvider {
    internal val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""CREATE TABLE IF NOT EXISTS asset_research_dossier_snapshots (
                snapshotId TEXT NOT NULL,
                assetId TEXT NOT NULL,
                generatedAtEpochMs INTEGER NOT NULL,
                schemaVersion INTEGER NOT NULL,
                evidenceStatus TEXT NOT NULL,
                payloadJson TEXT NOT NULL,
                payloadSha256 TEXT NOT NULL,
                PRIMARY KEY(snapshotId),
                FOREIGN KEY(assetId) REFERENCES assets(assetId)
                ON UPDATE NO ACTION ON DELETE RESTRICT
            )""")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_asset_research_dossier_snapshots_assetId_generatedAtEpochMs ON asset_research_dossier_snapshots (assetId, generatedAtEpochMs)")
        }
    }
    internal val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS global_market_snapshots (snapshotId TEXT NOT NULL PRIMARY KEY, sourceId TEXT NOT NULL, snapshotType TEXT NOT NULL, observedAtEpochMs INTEGER NOT NULL, retrievedAtEpochMs INTEGER NOT NULL, quality TEXT NOT NULL, payloadJson TEXT NOT NULL, payloadSha256 TEXT NOT NULL)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_global_market_snapshots_sourceId_snapshotType_retrievedAtEpochMs ON global_market_snapshots (sourceId, snapshotType, retrievedAtEpochMs)")
        }
    }

    @Volatile
    private var instance: IIEDatabase? = null

    fun get(context: Context): IIEDatabase =
        instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                IIEDatabase::class.java,
                "iie-local.db"
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also { instance = it }
        }
}
