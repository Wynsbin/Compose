package com.yung.iot.mqtt

sealed interface MqttConnectionState {
    data object Disconnected : MqttConnectionState
    data object Connecting : MqttConnectionState
    data class Connected(val subscribedTopics: List<String> = emptyList()) : MqttConnectionState
    data class Error(val message: String) : MqttConnectionState
}

data class MqttIncomingMessage(
    val topic: String,
    val payload: String,
    val timestamp: Long = System.currentTimeMillis(),
)
