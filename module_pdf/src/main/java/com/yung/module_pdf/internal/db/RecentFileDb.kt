package com.yung.module_pdf.internal.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yung.module_pdf.api.PdfSdk

@Database(entities = [FileInfoEntity::class], version = 1, exportSchema = false)
abstract class RecentFileDb : RoomDatabase() {

    abstract fun dao(): RecentFileDao

    companion object {
        @Volatile
        private var INSTANCE: RecentFileDb? = null

        private const val ROOM_DBNAME = "recent_file.db"

        fun getInstance(): RecentFileDb {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    PdfSdk.requireApp().applicationContext,
                    RecentFileDb::class.java,
                    ROOM_DBNAME
                ).build().apply { INSTANCE = this }

            }
        }
    }
}

