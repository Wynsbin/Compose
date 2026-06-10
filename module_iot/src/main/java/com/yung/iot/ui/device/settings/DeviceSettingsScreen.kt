package com.yung.iot.ui.device.settings

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceSettingsScreen(
    viewModel: DeviceSettingsViewModel,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    if (uiState.deleted) {
        onDeleted()
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("设备设置") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = uiState.editName,
                onValueChange = viewModel::updateName,
                label = { Text("设备名称") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Text("所属房间")
            uiState.rooms.forEach { room ->
                TextButton(
                    onClick = { viewModel.selectRoom(room.roomId) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val selected = uiState.selectedRoomId == room.roomId
                    Text(if (selected) "✓ ${room.name}" else room.name)
                }
            }
            uiState.device?.let { device ->
                Text("型号: ${device.productId}")
                Text("固件: ${device.fwVersion}")
                Text("MAC: ${device.mac}")
            }
            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (uiState.saved) "已保存" else "保存")
            }
            TextButton(
                onClick = viewModel::unbind,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("解绑设备", color = androidx.compose.material3.MaterialTheme.colorScheme.error)
            }
        }
    }
}
