package com.yung.iot.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {

    @Query("SELECT * FROM iot_devices WHERE homeId = :homeId ORDER BY name ASC")
    fun observeByHome(homeId: String): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM iot_devices WHERE deviceId = :deviceId LIMIT 1")
    fun observeById(deviceId: String): Flow<DeviceEntity?>

    @Query("SELECT * FROM iot_devices WHERE deviceId = :deviceId LIMIT 1")
    suspend fun getById(deviceId: String): DeviceEntity?

    @Query("SELECT * FROM iot_devices WHERE homeId = :homeId AND (:roomId IS NULL OR roomId = :roomId)")
    suspend fun getByHomeAndRoom(homeId: String, roomId: String?): List<DeviceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(devices: List<DeviceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(device: DeviceEntity)

    @Update
    suspend fun update(device: DeviceEntity)

    @Query("DELETE FROM iot_devices WHERE deviceId = :deviceId")
    suspend fun deleteById(deviceId: String)

    @Query("SELECT COUNT(*) FROM iot_devices")
    suspend fun count(): Int
}
