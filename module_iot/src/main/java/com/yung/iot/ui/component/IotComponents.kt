package com.yung.iot.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Outlet
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yung.iot.data.model.DeviceCategory
import com.yung.iot.data.model.IotDevice
import com.yung.iot.mqtt.MqttConnectionState

@Composable
fun ConnectionStatusBar(
    state: MqttConnectionState,
    modifier: Modifier = Modifier,
) {
    val (text, color) = when (state) {
        is MqttConnectionState.Connected -> "MQTT 已连接" to MaterialTheme.colorScheme.primary
        is MqttConnectionState.Connecting -> "MQTT 连接中..." to MaterialTheme.colorScheme.tertiary
        is MqttConnectionState.Disconnected -> "MQTT 未连接" to MaterialTheme.colorScheme.outline
        is MqttConnectionState.Error -> state.message to MaterialTheme.colorScheme.error
    }
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        style = MaterialTheme.typography.labelMedium,
        color = color,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeviceCard(
    device: IotDevice,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val alpha = if (device.online) 1f else 0.55f
    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = device.category.icon(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                OnlineDot(online = device.online)
            }
            Text(
                text = device.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = device.statusSummary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!device.online) {
                Text(
                    text = "离线",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            } else if (onLongClick != null && device.power != null) {
                Text(
                    text = if (device.power) "点击卡片控制 · 长按快捷开关" else "点击卡片控制",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun OnlineDot(online: Boolean) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .background(
                color = if (online) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                },
                shape = CircleShape,
            ),
    )
}

private fun DeviceCategory.icon(): ImageVector = when (this) {
    DeviceCategory.LIGHT -> Icons.Default.Lightbulb
    DeviceCategory.SOCKET -> Icons.Default.Outlet
    DeviceCategory.SENSOR -> Icons.Default.Sensors
    else -> Icons.Default.Sensors
}
