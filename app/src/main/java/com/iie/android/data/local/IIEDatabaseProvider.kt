package com.iie.android.data.local

import android.content.Context
import androidx.room.Room

/**
 * Process-wide database provider. The database is stored in app-private storage.
 * Destructive migrations are intentionally not enabled.
 */
object IIEDatabaseProvider {
    @Volatile
    private var instance: IIEDatabase? = null

    fun get(context: Context): IIEDatabase =
        instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                IIEDatabase::class.java,
                "iie-local.db"
            ).build().also { instance = it }
        }
}
