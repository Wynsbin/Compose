package com.yung.iot.ui.provision

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvisionScreen(
    viewModel: ProvisionViewModel,
    onBack: () -> Unit,
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    if (uiState.completed) {
        onCompleted()
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("添加设备") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.step == ProvisionStep.SELECT_METHOD) onBack() else viewModel.back()
                    }) {
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
            LinearProgressIndicator(
                progress = { (uiState.step.ordinal + 1) / 4f },
                modifier = Modifier.fillMaxWidth(),
            )
            when (uiState.step) {
                ProvisionStep.SELECT_METHOD -> SelectMethodStep(onSelect = viewModel::selectMethod)
                ProvisionStep.DISCOVER -> DiscoverStep(
                    method = uiState.method,
                    onNext = viewModel::nextFromDiscover,
                )
                ProvisionStep.CONFIGURE_WIFI -> ConfigureWifiStep(
                    isProcessing = uiState.isProcessing,
                    onStart = viewModel::simulateProvision,
                )
                ProvisionStep.COMPLETE -> CompleteStep(
                    deviceName = uiState.deviceName,
                    onNameChange = viewModel::updateDeviceName,
                    onFinish = viewModel::finishBinding,
                )
            }
            uiState.error?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun SelectMethodStep(onSelect: (ProvisionMethod) -> Unit) {
    Text("选择配网方式")
    Button(onClick = { onSelect(ProvisionMethod.BLE) }, modifier = Modifier.fillMaxWidth()) {
        Text("蓝牙 BLE 配网（灯/插座）")
    }
    Button(onClick = { onSelect(ProvisionMethod.AP) }, modifier = Modifier.fillMaxWidth()) {
        Text("AP 热点配网（摄像头/网关）")
    }
    Button(onClick = { onSelect(ProvisionMethod.QR) }, modifier = Modifier.fillMaxWidth()) {
        Text("扫码绑定（已联网设备）")
    }
}

@Composable
private fun DiscoverStep(method: ProvisionMethod?, onNext: () -> Unit) {
    Text(
        when (method) {
            ProvisionMethod.BLE -> "正在扫描 BLE 设备...\n（演示模式：点击下方继续）"
            ProvisionMethod.AP -> "请连接设备热点 SmartDevice_XXXX\n（演示模式：点击下方继续）"
            ProvisionMethod.QR -> "请扫描机身二维码\n（演示模式：点击下方继续）"
            null -> "请选择配网方式"
        },
    )
    Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) { Text("发现设备，下一步") }
}

@Composable
private fun ConfigureWifiStep(isProcessing: Boolean, onStart: () -> Unit) {
    OutlinedTextField(
        value = "Home_WiFi",
        onValueChange = {},
        label = { Text("Wi-Fi 名称") },
        modifier = Modifier.fillMaxWidth(),
        enabled = false,
    )
    OutlinedTextField(
        value = "********",
        onValueChange = {},
        label = { Text("Wi-Fi 密码") },
        modifier = Modifier.fillMaxWidth(),
        enabled = false,
    )
    if (isProcessing) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator()
            Text("设备配网中...")
        }
    } else {
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("开始配网") }
    }
}

@Composable
private fun CompleteStep(
    deviceName: String,
    onNameChange: (String) -> Unit,
    onFinish: () -> Unit,
) {
    Text("配网成功！请命名设备并选择房间")
    OutlinedTextField(
        value = deviceName,
        onValueChange = onNameChange,
        label = { Text("设备名称") },
        modifier = Modifier.fillMaxWidth(),
    )
    Text("默认添加到客厅，可在设备设置中修改房间")
    Button(onClick = onFinish, modifier = Modifier.fillMaxWidth()) { Text("完成") }
    TextButton(onClick = onFinish) { Text("跳过，使用默认名称") }
}
