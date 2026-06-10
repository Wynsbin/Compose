package com.yung.iot.ui.device.list

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
import com.yung.iot.ui.device.detail.DeviceDetailActivity
import com.yung.route.RoutePath

@Route(path = RoutePath.Iot.DEVICE_LIST)
class DeviceListActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!IotSdk.isInitialized()) IotSdk.init(application)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(color = Color.Transparent) {
                    val viewModel: DeviceListViewModel = viewModel()
                    DeviceListScreen(
                        viewModel = viewModel,
                        onBack = { finish() },
                        onDeviceClick = { deviceId ->
                            ARouter.getInstance()
                                .build(RoutePath.Iot.DEVICE_DETAIL)
                                .withString(DeviceDetailActivity.EXTRA_DEVICE_ID, deviceId)
                                .navigation(this)
                        },
                    )
                }
            }
        }
    }
}
