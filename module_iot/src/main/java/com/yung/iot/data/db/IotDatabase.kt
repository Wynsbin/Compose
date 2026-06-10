package com.yung.iot.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DeviceEntity::class, RoomEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class IotDatabase : RoomDatabase() {

    abstract fun deviceDao(): DeviceDao
    abstract fun roomDao(): RoomDao

    companion object {
        @Volatile
        private var instance: IotDatabase? = null

        fun get(context: Context): IotDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    IotDatabase::class.java,
                    "iot_smart_home.db",
                ).build().also { instance = it }
            }
        }
    }
}
