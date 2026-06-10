package com.yung.iot.ui.device.detail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.yung.iot.api.IotSdk
import com.yung.iot.ui.DeviceDetailViewModelFactory
import com.yung.iot.ui.device.settings.DeviceSettingsActivity
import com.yung.route.RoutePath

@Route(path = RoutePath.Iot.DEVICE_DETAIL)
class DeviceDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!IotSdk.isInitialized()) IotSdk.init(application)
        val deviceId = intent.getStringExtra(EXTRA_DEVICE_ID).orEmpty()
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(color = Color.Transparent) {
                    val viewModel: DeviceDetailViewModel = viewModel(
                        factory = DeviceDetailViewModelFactory(deviceId),
                    )
                    DeviceDetailScreen(
                        viewModel = viewModel,
                        onBack = { finish() },
                        onSettings = {
                            ARouter.getInstance()
                                .build(RoutePath.Iot.DEVICE_SETTINGS)
                                .withString(DeviceSettingsActivity.EXTRA_DEVICE_ID, deviceId)
                                .navigation(this)
                        },
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_DEVICE_ID = "extra_device_id"
    }
}
