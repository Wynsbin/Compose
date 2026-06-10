package com.yung.iot.data.repository

import android.content.Context
import com.yung.iot.data.db.DeviceEntity
import com.yung.iot.data.db.IotDatabase
import com.yung.iot.data.mapper.toEntity
import com.yung.iot.data.mapper.toModel
import com.yung.iot.data.mapper.withSummary
import com.yung.iot.data.model.DeviceCategory
import com.yung.iot.data.model.IotDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DeviceRepository private constructor(
    context: Context,
) {
    private val deviceDao = IotDatabase.get(context).deviceDao()

    fun observeDevices(homeId: String = HomeRepository.DEFAULT_HOME_ID): Flow<List<IotDevice>> {
        return deviceDao.observeByHome(homeId).map { list ->
            list.map { it.toModel().withSummary() }
        }
    }

    fun observeDevice(deviceId: String): Flow<IotDevice?> {
        return deviceDao.observeById(deviceId).map { entity ->
            entity?.toModel()?.withSummary()
        }
    }

    suspend fun getDevice(deviceId: String): IotDevice? {
        return deviceDao.getById(deviceId)?.toModel()?.withSummary()
    }

    suspend fun ensureSeedData() {
        if (deviceDao.count() > 0) return
        deviceDao.insertAll(
            listOf(
                seedDevice(
                    deviceId = "dev_light01",
                    roomId = "room_living",
                    productId = "light_rgb_v1",
                    name = "客厅吸顶灯",
                    category = DeviceCategory.LIGHT,
                    power = true,
                    brightness = 80,
                ),
                seedDevice(
                    deviceId = "dev_socket01",
                    roomId = "room_bedroom",
                    productId = "socket_v1",
                    name = "卧室插座",
                    category = DeviceCategory.SOCKET,
                    power = false,
                ),
                seedDevice(
                    deviceId = "dev_sensor01",
                    roomId = "room_balcony",
                    productId = "sensor_temp_humi_v1",
                    name = "阳台温湿度",
                    category = DeviceCategory.SENSOR,
                    temperature = 26.5f,
                    humidity = 65f,
                ),
            ),
        )
    }

    suspend fun updateFromMqtt(update: IncomingDeviceUpdate) {
        val current = deviceDao.getById(update.deviceId) ?: return
        val updated = current.copy(
            online = update.online,
            power = update.params["power"] as? Boolean ?: current.power,
            brightness = (update.params["brightness"] as? Number)?.toInt() ?: current.brightness,
            temperature = (update.params["temperature"] as? Number)?.toFloat() ?: current.temperature,
            humidity = (update.params["humidity"] as? Number)?.toFloat() ?: current.humidity,
        ).toModel().withSummary().toEntity()
        deviceDao.update(updated)
    }

    suspend fun updateDevice(device: IotDevice) {
        deviceDao.update(device.withSummary().toEntity())
    }

    suspend fun togglePower(deviceId: String, power: Boolean) {
        val device = getDevice(deviceId) ?: return
        updateDevice(device.copy(power = power))
    }

    suspend fun updateBrightness(deviceId: String, brightness: Int) {
        val device = getDevice(deviceId) ?: return
        updateDevice(device.copy(brightness = brightness))
    }

    suspend fun renameDevice(deviceId: String, name: String) {
        val device = getDevice(deviceId) ?: return
        updateDevice(device.copy(name = name))
    }

    suspend fun assignRoom(deviceId: String, roomId: String?) {
        val device = getDevice(deviceId) ?: return
        updateDevice(device.copy(roomId = roomId))
    }

    suspend fun deleteDevice(deviceId: String) {
        deviceDao.deleteById(deviceId)
    }

    suspend fun addDevice(device: IotDevice) {
        deviceDao.insert(device.withSummary().toEntity())
    }

    private fun seedDevice(
        deviceId: String,
        roomId: String,
        productId: String,
        name: String,
        category: DeviceCategory,
        power: Boolean? = null,
        brightness: Int? = null,
        temperature: Float? = null,
        humidity: Float? = null,
    ): DeviceEntity {
        return IotDevice(
            deviceId = deviceId,
            homeId = HomeRepository.DEFAULT_HOME_ID,
            roomId = roomId,
            productId = productId,
            name = name,
            category = category,
            online = true,
            power = power,
            brightness = brightness,
            temperature = temperature,
            humidity = humidity,
            fwVersion = "1.0.0",
            mac = "AA:BB:CC:DD:EE:FF",
        ).withSummary().toEntity()
    }

    companion object {
        @Volatile
        private var instance: DeviceRepository? = null

        fun init(context: Context) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = DeviceRepository(context.applicationContext)
                    }
                }
            }
        }

        fun getInstance(): DeviceRepository {
            return instance ?: error("DeviceRepository.init() must be called first")
        }
    }
}
