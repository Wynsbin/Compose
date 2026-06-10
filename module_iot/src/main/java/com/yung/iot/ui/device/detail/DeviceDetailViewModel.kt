package com.yung.iot.ui.device.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yung.iot.data.model.IotDevice
import com.yung.iot.data.model.ProductTemplate
import com.yung.iot.data.model.ProductTemplates
import com.yung.iot.data.repository.DeviceRepository
import com.yung.iot.data.repository.MqttRepository
import com.yung.iot.mqtt.MqttConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DeviceDetailUiState(
    val device: IotDevice? = null,
    val template: ProductTemplate? = null,
    val mqttState: MqttConnectionState = MqttConnectionState.Disconnected,
    val controlError: String? = null,
)

class DeviceDetailViewModel(
    private val deviceId: String,
    private val deviceRepository: DeviceRepository = DeviceRepository.getInstance(),
    private val mqttRepository: MqttRepository = MqttRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceDetailUiState())
    val uiState: StateFlow<DeviceDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            deviceRepository.observeDevice(deviceId).collect { device ->
                _uiState.update {
                    it.copy(
                        device = device,
                        template = device?.let { d -> ProductTemplates.find(d.productId) },
                    )
                }
            }
        }
        viewModelScope.launch {
            mqttRepository.connectionState.collect { state ->
                _uiState.update { it.copy(mqttState = state) }
            }
        }
        viewModelScope.launch {
            mqttRepository.incomingMessages.collect { message ->
                mqttRepository.parseIncoming(message)?.let { update ->
                    if (update.deviceId == deviceId) {
                        deviceRepository.updateFromMqtt(update)
                    }
                }
            }
        }
    }

    fun setPower(power: Boolean) {
        val device = _uiState.value.device ?: return
        if (!device.online) {
            _uiState.update { it.copy(controlError = "设备已离线") }
            return
        }
        viewModelScope.launch {
            deviceRepository.togglePower(deviceId, power)
            mqttRepository.publishPropertySet(device.copy(power = power), mapOf("power" to power))
            _uiState.update { it.copy(controlError = null) }
        }
    }

    fun setBrightness(brightness: Int) {
        val device = _uiState.value.device ?: return
        if (!device.online) {
            _uiState.update { it.copy(controlError = "设备已离线") }
            return
        }
        viewModelScope.launch {
            deviceRepository.updateBrightness(deviceId, brightness)
            mqttRepository.publishPropertySet(
                device.copy(brightness = brightness),
                mapOf("brightness" to brightness),
            )
            _uiState.update { it.copy(controlError = null) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(controlError = null) }
    }
}
