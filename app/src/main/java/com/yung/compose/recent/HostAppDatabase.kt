package com.yung.compose.recent

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [HostRecentFileEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(HostRecentFileConverters::class)
abstract class HostAppDatabase : RoomDatabase() {

    abstract fun recentFileDao(): HostRecentFileDao

    companion object {
        @Volatile
        private var instance: HostAppDatabase? = null

        fun get(context: Context): HostAppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    HostAppDatabase::class.java,
                    "host_app.db",
                ).build().also { instance = it }
            }
        }
    }
}
