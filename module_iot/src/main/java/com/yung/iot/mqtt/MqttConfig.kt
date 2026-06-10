package com.yung.iot.mqtt

data class MqttConfig(
    val serverUri: String,
    val username: String,
    val password: String,
    val clientId: String,
    val legacyTopic: String,
) {
    /** Paho：仅 ssl:// / wss:// 可设置 SSLSocketFactory，tcp:// 不可设置 */
    val useTls: Boolean
        get() = serverUri.startsWith("ssl://", ignoreCase = true) ||
            serverUri.startsWith("wss://", ignoreCase = true)

    companion object {
        val DEFAULT = MqttConfig(
            serverUri = "ssl://hfc4c6dd.ala.dedicated.aliyun.emqxcloud.cn:8883",
            username = "yung",
            password = "123456",
            clientId = "android_iot_client",
            legacyTopic = "a/b",
        )
    }
}
