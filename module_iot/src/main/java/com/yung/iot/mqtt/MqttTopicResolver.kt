package com.yung.iot.mqtt

object MqttTopicResolver {

    fun propertyPostTopic(productId: String, deviceId: String): String =
        "iot/$productId/$deviceId/property/post"

    fun propertySetTopic(productId: String, deviceId: String): String =
        "iot/$productId/$deviceId/property/set"

    fun stateTopic(productId: String, deviceId: String): String =
        "iot/$productId/$deviceId/state"

    fun propertyPostWildcard(): String = "iot/+/+/property/post"

    fun stateWildcard(): String = "iot/+/+/state"
}
