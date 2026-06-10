package com.yung.iot.api

import android.app.Application
import com.yung.iot.data.repository.DeviceRepository
import com.yung.iot.data.repository.HomeRepository
import com.yung.iot.mqtt.MqttConfig
import com.yung.iot.mqtt.MqttConnectionManager

object IotSdk {

    @Volatile
    private var application: Application? = null

    fun init(
        application: Application,
        mqttConfig: MqttConfig = MqttConfig.DEFAULT,
    ) {
        this.application = application
        MqttConnectionManager.init(application, mqttConfig)
        HomeRepository.init(application)
        DeviceRepository.init(application)
    }

    fun requireApp(): Application {
        return application ?: error("IotSdk.init() must be called before using module_iot")
    }

    fun isInitialized(): Boolean = application != null
}
