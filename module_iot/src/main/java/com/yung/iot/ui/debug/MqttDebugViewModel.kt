package com.yung.iot.ui.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yung.iot.data.repository.MqttRepository
import com.yung.iot.mqtt.MqttConfig
import com.yung.iot.mqtt.MqttConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MqttDebugUiState(
    val connectionState: MqttConnectionState = MqttConnectionState.Disconnected,
    val lastMessage: String = "暂无消息",
    val isDisconnecting: Boolean = false,
    val legacyTopic: String = MqttConfig.DEFAULT.legacyTopic,
)

class MqttDebugViewModel(
    private val mqttRepository: MqttRepository = MqttRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(MqttDebugUiState())
    val uiState: StateFlow<MqttDebugUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            mqttRepository.connectionState.collect { state ->
                _uiState.update {
                    it.copy(
                        connectionState = state,
                        isDisconnecting = false,
                    )
                }
            }
        }
        viewModelScope.launch {
            mqttRepository.incomingMessages.collect { message ->
                _uiState.update {
                    it.copy(lastMessage = "${message.topic} -> ${message.payload}")
                }
            }
        }
        viewModelScope.launch {
            mqttRepository.lastPublished.collect { published ->
                if (published != null) {
                    _uiState.update { it.copy(lastMessage = published) }
                }
            }
        }
        connect()
    }

    fun connect() {
        mqttRepository.connect()
    }

    fun publish() {
        mqttRepository.publishLegacy("Hello from Android", _uiState.value.legacyTopic)
    }

    fun disconnect() {
        _uiState.update { it.copy(isDisconnecting = true) }
        mqttRepository.disconnect()
    }

    fun statusText(): String = when (val state = _uiState.value.connectionState) {
        is MqttConnectionState.Disconnected -> "未连接"
        is MqttConnectionState.Connecting -> "连接中..."
        is MqttConnectionState.Connected -> {
            val topics = state.subscribedTopics.joinToString()
            if (topics.isEmpty()) "已连接" else "已连接，已订阅 $topics"
        }
        is MqttConnectionState.Error -> state.message
    }
}
