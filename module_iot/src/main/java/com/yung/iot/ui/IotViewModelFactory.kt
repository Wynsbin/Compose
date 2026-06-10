package com.yung.iot.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yung.iot.ui.device.detail.DeviceDetailViewModel
import com.yung.iot.ui.device.settings.DeviceSettingsViewModel

class DeviceDetailViewModelFactory(
    private val deviceId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeviceDetailViewModel::class.java)) {
            return DeviceDetailViewModel(deviceId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

class DeviceSettingsViewModelFactory(
    private val deviceId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeviceSettingsViewModel::class.java)) {
            return DeviceSettingsViewModel(deviceId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
