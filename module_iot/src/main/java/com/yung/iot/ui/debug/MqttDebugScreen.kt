package com.yung.iot.ui.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MqttDebugScreen(
    viewModel: MqttDebugViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("MQTT 调试") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        ) {
            Text(text = "IoT MQTT 调试面板")
            Text(text = "状态: ${viewModel.statusText()}")
            Text(
                text = "最近消息: ${uiState.lastMessage}",
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "主题: ${uiState.legacyTopic}",
                modifier = Modifier.fillMaxWidth(),
            )
            Button(onClick = viewModel::publish) {
                Text(text = "发布消息")
            }
            Button(
                onClick = viewModel::disconnect,
                enabled = !uiState.isDisconnecting,
            ) {
                Text(text = if (uiState.isDisconnecting) "断开中..." else "断开连接")
            }
            Button(onClick = viewModel::connect) {
                Text(text = "重新连接")
            }
        }
    }
}
