package com.yung.iot.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yung.iot.data.model.IotDevice
import com.yung.iot.data.model.IotRoom
import com.yung.iot.data.repository.DeviceRepository
import com.yung.iot.data.repository.HomeRepository
import com.yung.iot.data.repository.MqttRepository
import com.yung.iot.mqtt.MqttConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IotMainUiState(
    val homeName: String = "我的家",
    val rooms: List<IotRoom> = emptyList(),
    val selectedRoomId: String? = null,
    val devices: List<IotDevice> = emptyList(),
    val filteredDevices: List<IotDevice> = emptyList(),
    val mqttState: MqttConnectionState = MqttConnectionState.Disconnected,
    val isRefreshing: Boolean = false,
)

class IotMainViewModel(
    private val homeRepository: HomeRepository = HomeRepository.getInstance(),
    private val deviceRepository: DeviceRepository = DeviceRepository.getInstance(),
    private val mqttRepository: MqttRepository = MqttRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(IotMainUiState())
    val uiState: StateFlow<IotMainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            homeRepository.ensureSeedData()
            deviceRepository.ensureSeedData()
            mqttRepository.connect()
        }
        observeData()
        observeMqtt()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                homeRepository.observeRooms(),
                deviceRepository.observeDevices(),
            ) { rooms, devices ->
                val selectedRoomId = _uiState.value.selectedRoomId
                Triple(rooms, devices, filterDevices(devices, selectedRoomId))
            }.collect { (rooms, devices, filtered) ->
                _uiState.update {
                    it.copy(
                        homeName = homeRepository.defaultHome.name,
                        rooms = rooms,
                        devices = devices,
                        filteredDevices = filtered,
                    )
                }
            }
        }
    }

    private fun observeMqtt() {
        viewModelScope.launch {
            mqttRepository.connectionState.collect { state ->
                _uiState.update { it.copy(mqttState = state) }
            }
        }
        viewModelScope.launch {
            mqttRepository.incomingMessages.collect { message ->
                mqttRepository.parseIncoming(message)?.let { update ->
                    deviceRepository.updateFromMqtt(update)
                }
            }
        }
    }

    fun selectRoom(roomId: String?) {
        _uiState.update { state ->
            state.copy(
                selectedRoomId = roomId,
                filteredDevices = filterDevices(state.devices, roomId),
            )
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            mqttRepository.connect()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun toggleDevicePower(deviceId: String) {
        viewModelScope.launch {
            val device = deviceRepository.getDevice(deviceId) ?: return@launch
            if (!device.online || device.power == null) return@launch
            val newPower = !device.power
            deviceRepository.togglePower(deviceId, newPower)
            mqttRepository.publishPropertySet(device, mapOf("power" to newPower))
        }
    }

    private fun filterDevices(devices: List<IotDevice>, roomId: String?): List<IotDevice> {
        if (roomId == null) return devices
        return devices.filter { it.roomId == roomId }
    }
}
