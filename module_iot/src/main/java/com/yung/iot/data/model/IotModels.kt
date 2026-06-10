package com.yung.iot.data.model

enum class DeviceCategory {
    LIGHT,
    SOCKET,
    SENSOR,
    CAMERA,
    GATEWAY,
}

data class IotRoom(
    val roomId: String,
    val homeId: String,
    val name: String,
    val sortOrder: Int,
)

data class IotHome(
    val homeId: String,
    val name: String,
)

data class IotDevice(
    val deviceId: String,
    val homeId: String,
    val roomId: String?,
    val productId: String,
    val name: String,
    val category: DeviceCategory,
    val online: Boolean,
    val power: Boolean? = null,
    val brightness: Int? = null,
    val temperature: Float? = null,
    val humidity: Float? = null,
    val statusSummary: String = "",
    val fwVersion: String = "1.0.0",
    val mac: String = "",
)

data class ProductPropertyDef(
    val key: String,
    val label: String,
    val type: PropertyType,
    val min: Int? = null,
    val max: Int? = null,
)

enum class PropertyType {
    SWITCH,
    SLIDER,
    READONLY,
}

data class ProductTemplate(
    val productId: String,
    val name: String,
    val category: DeviceCategory,
    val properties: List<ProductPropertyDef>,
)

object ProductTemplates {
    val LIGHT = ProductTemplate(
        productId = "light_rgb_v1",
        name = "智能灯",
        category = DeviceCategory.LIGHT,
        properties = listOf(
            ProductPropertyDef("power", "开关", PropertyType.SWITCH),
            ProductPropertyDef("brightness", "亮度", PropertyType.SLIDER, 0, 100),
        ),
    )

    val SOCKET = ProductTemplate(
        productId = "socket_v1",
        name = "智能插座",
        category = DeviceCategory.SOCKET,
        properties = listOf(
            ProductPropertyDef("power", "开关", PropertyType.SWITCH),
        ),
    )

    val SENSOR = ProductTemplate(
        productId = "sensor_temp_humi_v1",
        name = "温湿度传感器",
        category = DeviceCategory.SENSOR,
        properties = listOf(
            ProductPropertyDef("temperature", "温度", PropertyType.READONLY),
            ProductPropertyDef("humidity", "湿度", PropertyType.READONLY),
        ),
    )

    fun find(productId: String): ProductTemplate? = listOf(LIGHT, SOCKET, SENSOR)
        .firstOrNull { it.productId == productId }
}
