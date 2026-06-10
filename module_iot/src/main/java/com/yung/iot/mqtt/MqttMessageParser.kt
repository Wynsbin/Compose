package com.yung.iot.mqtt

import org.json.JSONObject

object MqttMessageParser {

    fun parsePropertyParams(payload: String): Map<String, Any?> {
        return runCatching {
            val json = JSONObject(payload)
            val params = json.optJSONObject("params") ?: return@runCatching emptyMap()
            params.keys().asSequence().associateWith { key ->
                when (val value = params.get(key)) {
                    is Boolean, is Int, is Long, is Double, is String -> value
                    else -> value.toString()
                }
            }
        }.getOrDefault(emptyMap())
    }

    fun buildPropertySetPayload(params: Map<String, Any?>): String {
        val paramsJson = JSONObject()
        params.forEach { (key, value) ->
            when (value) {
                null -> Unit
                is Boolean -> paramsJson.put(key, value)
                is Number -> paramsJson.put(key, value)
                else -> paramsJson.put(key, value.toString())
            }
        }
        return JSONObject()
            .put("id", "set_${System.currentTimeMillis()}")
            .put("version", "1.0")
            .put("method", "property.set")
            .put("params", paramsJson)
            .put("timestamp", System.currentTimeMillis())
            .toString()
    }

    fun parseTopicIdentity(topic: String): Pair<String, String>? {
        val parts = topic.split("/")
        if (parts.size < 4 || parts[0] != "iot") return null
        return parts[1] to parts[2]
    }
}
