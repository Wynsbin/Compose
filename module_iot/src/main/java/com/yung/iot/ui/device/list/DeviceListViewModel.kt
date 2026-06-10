package com.yung.iot.ui.device.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yung.iot.data.model.IotDevice
import com.yung.iot.data.repository.DeviceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OnlineFilter {
    ALL,
    ONLINE,
    OFFLINE,
}

data class DeviceListUiState(
    val devices: List<IotDevice> = emptyList(),
    val filter: OnlineFilter = OnlineFilter.ALL,
    val filteredDevices: List<IotDevice> = emptyList(),
)

class DeviceListViewModel(
    private val deviceRepository: DeviceRepository = DeviceRepository.getInstance(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceListUiState())
    val uiState: StateFlow<DeviceListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            deviceRepository.observeDevices().collect { devices ->
                _uiState.update { state ->
                    state.copy(
                        devices = devices,
                        filteredDevices = applyFilter(devices, state.filter),
                    )
                }
            }
        }
    }

    fun setFilter(filter: OnlineFilter) {
        _uiState.update { state ->
            state.copy(
                filter = filter,
                filteredDevices = applyFilter(state.devices, filter),
            )
        }
    }

    private fun applyFilter(devices: List<IotDevice>, filter: OnlineFilter): List<IotDevice> {
        return when (filter) {
            OnlineFilter.ALL -> devices
            OnlineFilter.ONLINE -> devices.filter { it.online }
            OnlineFilter.OFFLINE -> devices.filter { !it.online }
        }
    }
}
