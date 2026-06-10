package com.yung.iot.ui.home

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

@Route(path = RoutePath.Iot.MAIN)
class IotMainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ensureIotInitialized()
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(color = Color.Transparent) {
                    val viewModel: IotMainViewModel = viewModel()
                    IotMainScreen(
                        viewModel = viewModel,
                        onAddDevice = {
                            ARouter.getInstance()
                                .build(RoutePath.Iot.PROVISION_START)
                                .navigation(this)
                        },
                        onDeviceList = {
                            ARouter.getInstance()
                                .build(RoutePath.Iot.DEVICE_LIST)
                                .navigation(this)
                        },
                        onDeviceClick = { deviceId ->
                            ARouter.getInstance()
                                .build(RoutePath.Iot.DEVICE_DETAIL)
                                .withString(DeviceDetailActivity.EXTRA_DEVICE_ID, deviceId)
                                .navigation(this)
                        },
                        onDebug = {
                            ARouter.getInstance()
                                .build(RoutePath.Iot.DEBUG_MQTT)
                                .navigation(this)
                        },
                    )
                }
            }
        }
    }

    private fun ensureIotInitialized() {
        if (!IotSdk.isInitialized()) {
            IotSdk.init(application)
        }
    }
}
