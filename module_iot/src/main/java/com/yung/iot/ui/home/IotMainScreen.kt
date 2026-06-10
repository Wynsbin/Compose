package com.yung.iot.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.yung.iot.data.model.DeviceCategory
import com.yung.iot.data.model.IotDevice
import com.yung.iot.ui.component.ConnectionStatusBar
import com.yung.iot.ui.component.DeviceCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IotMainScreen(
    viewModel: IotMainViewModel,
    onAddDevice: () -> Unit,
    onDeviceList: () -> Unit,
    onDeviceClick: (String) -> Unit,
    onDebug: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val roomTabs = listOf(null to "全部") + uiState.rooms.map { it.roomId to it.name }
    val selectedIndex = roomTabs.indexOfFirst { it.first == uiState.selectedRoomId }.coerceAtLeast(0)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(uiState.homeName) },
                actions = {
                    IconButton(onClick = onDeviceList) {
                        Icon(Icons.Default.List, contentDescription = "设备列表")
                    }
                    IconButton(onClick = onDebug) {
                        Icon(Icons.Default.BugReport, contentDescription = "MQTT 调试")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddDevice) {
                Icon(Icons.Default.Add, contentDescription = "添加设备")
            }
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize()) {
                ConnectionStatusBar(state = uiState.mqttState)
                ScrollableTabRow(selectedTabIndex = selectedIndex) {
                    roomTabs.forEachIndexed { index, (roomId, label) ->
                        Tab(
                            selected = selectedIndex == index,
                            onClick = { viewModel.selectRoom(roomId) },
                            text = { Text(label) },
                        )
                    }
                }
                if (uiState.filteredDevices.isEmpty()) {
                    Text(
                        text = "暂无设备，点击右下角添加",
                        modifier = Modifier.padding(24.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        items(uiState.filteredDevices, key = { it.deviceId }) { device ->
                            DeviceCard(
                                device = device,
                                onClick = { onDeviceClick(device.deviceId) },
                                onLongClick = if (
                                    device.category == DeviceCategory.LIGHT ||
                                    device.category == DeviceCategory.SOCKET
                                ) {
                                    { viewModel.toggleDevicePower(device.deviceId) }
                                } else {
                                    null
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
