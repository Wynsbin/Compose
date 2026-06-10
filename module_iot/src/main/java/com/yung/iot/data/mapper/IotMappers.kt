package com.yung.iot.data.mapper

import com.yung.iot.data.db.DeviceEntity
import com.yung.iot.data.db.RoomEntity
import com.yung.iot.data.model.DeviceCategory
import com.yung.iot.data.model.IotDevice
import com.yung.iot.data.model.IotRoom

fun DeviceEntity.toModel(): IotDevice = IotDevice(
    deviceId = deviceId,
    homeId = homeId,
    roomId = roomId,
    productId = productId,
    name = name,
    category = DeviceCategory.valueOf(category),
    online = online,
    power = power,
    brightness = brightness,
    temperature = temperature,
    humidity = humidity,
    statusSummary = statusSummary,
    fwVersion = fwVersion,
    mac = mac,
)

fun IotDevice.toEntity(): DeviceEntity = DeviceEntity(
    deviceId = deviceId,
    homeId = homeId,
    roomId = roomId,
    productId = productId,
    name = name,
    category = category.name,
    online = online,
    power = power,
    brightness = brightness,
    temperature = temperature,
    humidity = humidity,
    statusSummary = statusSummary,
    fwVersion = fwVersion,
    mac = mac,
)

fun RoomEntity.toModel(): IotRoom = IotRoom(
    roomId = roomId,
    homeId = homeId,
    name = name,
    sortOrder = sortOrder,
)

fun IotDevice.withSummary(): IotDevice {
    val summary = when (category) {
        DeviceCategory.LIGHT, DeviceCategory.SOCKET -> if (power == true) "开启" else "关闭"
        DeviceCategory.SENSOR -> {
            val temp = temperature?.let { "${it}°C" } ?: "--"
            val hum = humidity?.let { "${it.toInt()}%" } ?: "--"
            "$temp · $hum"
        }
        else -> statusSummary
    }
    return copy(statusSummary = summary)
}
