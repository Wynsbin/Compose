package com.yung.iot.ui.device.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yung.iot.data.model.DeviceCategory
import com.yung.iot.data.model.PropertyType
import com.yung.iot.ui.component.ConnectionStatusBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(
    viewModel: DeviceDetailViewModel,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val device = uiState.device

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(device?.name ?: "设备详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            ConnectionStatusBar(state = uiState.mqttState)
            if (device == null) {
                Text("设备不存在", modifier = Modifier.padding(24.dp))
                return@Column
            }
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = if (device.online) "在线 · ${device.statusSummary}" else "离线",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (device.online) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
                uiState.controlError?.let { error ->
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }
                when (device.category) {
                    DeviceCategory.LIGHT, DeviceCategory.SOCKET -> {
                        PowerControl(
                            power = device.power == true,
                            enabled = device.online,
                            onPowerChange = viewModel::setPower,
                        )
                        if (device.category == DeviceCategory.LIGHT) {
                            BrightnessControl(
                                brightness = device.brightness ?: 0,
                                enabled = device.online && device.power == true,
                                onBrightnessChange = viewModel::setBrightness,
                            )
                        }
                    }
                    DeviceCategory.SENSOR -> {
                        SensorReadings(
                            temperature = device.temperature,
                            humidity = device.humidity,
                        )
                    }
                    else -> {
                        Text("暂不支持该品类控制面板")
                    }
                }
                uiState.template?.properties?.forEach { property ->
                    if (property.type == PropertyType.READONLY) {
                        val value = when (property.key) {
                            "temperature" -> device.temperature?.let { "$it°C" }
                            "humidity" -> device.humidity?.let { "$it%" }
                            else -> null
                        }
                        if (value != null && device.category != DeviceCategory.SENSOR) {
                            Text("${property.label}: $value")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PowerControl(
    power: Boolean,
    enabled: Boolean,
    onPowerChange: (Boolean) -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Text("开关")
        Switch(
            checked = power,
            onCheckedChange = onPowerChange,
            enabled = enabled,
        )
    }
}

@Composable
private fun BrightnessControl(
    brightness: Int,
    enabled: Boolean,
    onBrightnessChange: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("亮度: $brightness%")
        Slider(
            value = brightness.toFloat(),
            onValueChange = { onBrightnessChange(it.toInt()) },
            valueRange = 0f..100f,
            enabled = enabled,
        )
    }
}

@Composable
private fun SensorReadings(
    temperature: Float?,
    humidity: Float?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "温度: ${temperature?.let { "$it°C" } ?: "--"}",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "湿度: ${humidity?.let { "${it.toInt()}%" } ?: "--"}",
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}
