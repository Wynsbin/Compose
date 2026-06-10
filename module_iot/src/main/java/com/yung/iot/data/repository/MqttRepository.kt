package com.yung.iot.data.repository

import com.yung.iot.data.model.IotDevice
import com.yung.iot.mqtt.MqttConnectionManager
import com.yung.iot.mqtt.MqttConnectionState
import com.yung.iot.mqtt.MqttIncomingMessage
import com.yung.iot.mqtt.MqttMessageParser
import com.yung.iot.mqtt.MqttTopicResolver
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class MqttRepository(
    private val manager: MqttConnectionManager = MqttConnectionManager.getInstance(),
) {
    val connectionState: StateFlow<MqttConnectionState> = manager.connectionState
    val incomingMessages: SharedFlow<MqttIncomingMessage> = manager.incomingMessages
    val lastPublished: StateFlow<String?> = manager.lastPublished

    fun connect() = manager.connect()

    fun disconnect() = manager.disconnect()

    fun publishLegacy(message: String, topic: String = "a/b") {
        manager.publish(topic = topic, payload = message)
    }

    fun publishPropertySet(device: IotDevice, params: Map<String, Any?>) {
        val topic = MqttTopicResolver.propertySetTopic(device.productId, device.deviceId)
        val payload = MqttMessageParser.buildPropertySetPayload(params)
        manager.publish(topic, payload)
    }

    fun parseIncoming(message: MqttIncomingMessage): IncomingDeviceUpdate? {
        val identity = MqttMessageParser.parseTopicIdentity(message.topic) ?: return null
        val (productId, deviceId) = identity
        val params = MqttMessageParser.parsePropertyParams(message.payload)
        if (params.isEmpty()) return null
        return IncomingDeviceUpdate(
            productId = productId,
            deviceId = deviceId,
            params = params,
            online = message.topic.endsWith("/state").not(),
        )
    }
}

data class IncomingDeviceUpdate(
    val productId: String,
    val deviceId: String,
    val params: Map<String, Any?>,
    val online: Boolean = true,
)
