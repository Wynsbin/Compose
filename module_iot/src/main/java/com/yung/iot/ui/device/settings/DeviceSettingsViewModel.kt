package com.yung.iot.ui.device.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yung.iot.data.model.IotDevice
import com.yung.iot.data.model.IotRoom
import com.yung.iot.data.repository.DeviceRepository
import com.yung.iot.data.repository.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DeviceSettingsUiState(
    val device: IotDevice? = null,
    val rooms: List<IotRoom> = emptyList(),
    val editName: String = "",
    val selectedRoomId: String? = null,
    val saved: Boolean = false,
    val deleted: Boolean = false,
)

class DeviceSettingsViewModel(
    private val deviceId: String,
    private val deviceRepository: DeviceRepository = DeviceRepository.getInstance(),
    private val homeRepository: HomeRepository = HomeRepository.getInstance(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceSettingsUiState())
    val uiState: StateFlow<DeviceSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            homeRepository.observeRooms().collect { rooms ->
                _uiState.update { it.copy(rooms = rooms) }
            }
        }
        viewModelScope.launch {
            deviceRepository.observeDevice(deviceId).collect { device ->
                _uiState.update {
                    it.copy(
                        device = device,
                        editName = device?.name ?: it.editName,
                        selectedRoomId = device?.roomId ?: it.selectedRoomId,
                    )
                }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(editName = name, saved = false) }
    }

    fun selectRoom(roomId: String?) {
        _uiState.update { it.copy(selectedRoomId = roomId, saved = false) }
    }

    fun save() {
        viewModelScope.launch {
            val name = _uiState.value.editName.trim()
            if (name.isNotEmpty()) {
                deviceRepository.renameDevice(deviceId, name)
            }
            deviceRepository.assignRoom(deviceId, _uiState.value.selectedRoomId)
            _uiState.update { it.copy(saved = true) }
        }
    }

    fun unbind() {
        viewModelScope.launch {
            deviceRepository.deleteDevice(deviceId)
            _uiState.update { it.copy(deleted = true) }
        }
    }
}
