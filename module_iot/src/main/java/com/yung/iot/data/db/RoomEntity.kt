package com.yung.iot.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "iot_rooms")
data class RoomEntity(
    @PrimaryKey val roomId: String,
    val homeId: String,
    val name: String,
    val sortOrder: Int,
)
