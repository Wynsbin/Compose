package com.yung.iot.ui.provision

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yung.iot.data.model.DeviceCategory
import com.yung.iot.data.model.IotDevice
import com.yung.iot.data.repository.DeviceRepository
import com.yung.iot.data.repository.HomeRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ProvisionMethod {
    BLE,
    AP,
    QR,
}

enum class ProvisionStep {
    SELECT_METHOD,
    DISCOVER,
    CONFIGURE_WIFI,
    COMPLETE,
}

data class ProvisionUiState(
    val step: ProvisionStep = ProvisionStep.SELECT_METHOD,
    val method: ProvisionMethod? = null,
    val deviceName: String = "新设备",
    val selectedRoomId: String = "room_living",
    val isProcessing: Boolean = false,
    val error: String? = null,
    val completed: Boolean = false,
)

class ProvisionViewModel(
    private val deviceRepository: DeviceRepository = DeviceRepository.getInstance(),
    private val homeRepository: HomeRepository = HomeRepository.getInstance(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProvisionUiState())
    val uiState: StateFlow<ProvisionUiState> = _uiState.asStateFlow()

    fun selectMethod(method: ProvisionMethod) {
        _uiState.update {
            it.copy(method = method, step = ProvisionStep.DISCOVER, error = null)
        }
    }

    fun nextFromDiscover() {
        _uiState.update { it.copy(step = ProvisionStep.CONFIGURE_WIFI, error = null) }
    }

    fun simulateProvision() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }
            delay(1500)
            _uiState.update { it.copy(step = ProvisionStep.COMPLETE, isProcessing = false) }
        }
    }

    fun updateDeviceName(name: String) {
        _uiState.update { it.copy(deviceName = name) }
    }

    fun selectRoom(roomId: String) {
        _uiState.update { it.copy(selectedRoomId = roomId) }
    }

    fun finishBinding() {
        viewModelScope.launch {
            val state = _uiState.value
            val productId = when (state.method) {
                ProvisionMethod.BLE -> "light_rgb_v1"
                ProvisionMethod.AP -> "socket_v1"
                ProvisionMethod.QR -> "sensor_temp_humi_v1"
                null -> "light_rgb_v1"
            }
            val category = when (productId) {
                "socket_v1" -> DeviceCategory.SOCKET
                "sensor_temp_humi_v1" -> DeviceCategory.SENSOR
                else -> DeviceCategory.LIGHT
            }
            val deviceId = "dev_${System.currentTimeMillis()}"
            deviceRepository.addDevice(
                IotDevice(
                    deviceId = deviceId,
                    homeId = HomeRepository.DEFAULT_HOME_ID,
                    roomId = state.selectedRoomId,
                    productId = productId,
                    name = state.deviceName.ifBlank { "新设备" },
                    category = category,
                    online = true,
                    power = if (category != DeviceCategory.SENSOR) false else null,
                    brightness = if (category == DeviceCategory.LIGHT) 50 else null,
                    temperature = if (category == DeviceCategory.SENSOR) 25f else null,
                    humidity = if (category == DeviceCategory.SENSOR) 60f else null,
                ),
            )
            _uiState.update { it.copy(completed = true) }
        }
    }

    fun back() {
        _uiState.update { state ->
            when (state.step) {
                ProvisionStep.SELECT_METHOD -> state
                ProvisionStep.DISCOVER -> state.copy(step = ProvisionStep.SELECT_METHOD)
                ProvisionStep.CONFIGURE_WIFI -> state.copy(step = ProvisionStep.DISCOVER)
                ProvisionStep.COMPLETE -> state.copy(step = ProvisionStep.CONFIGURE_WIFI)
            }
        }
    }
}
