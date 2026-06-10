package com.yung.iot.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "iot_devices")
data class DeviceEntity(
    @PrimaryKey val deviceId: String,
    val homeId: String,
    val roomId: String?,
    val productId: String,
    val name: String,
    val category: String,
    val online: Boolean,
    val power: Boolean?,
    val brightness: Int?,
    val temperature: Float?,
    val humidity: Float?,
    val statusSummary: String,
    val fwVersion: String,
    val mac: String,
)
