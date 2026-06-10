package com.yung.iot.ui.provision

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alibaba.android.arouter.facade.annotation.Route
import com.yung.iot.api.IotSdk
import com.yung.route.RoutePath

@Route(path = RoutePath.Iot.PROVISION_START)
class ProvisionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!IotSdk.isInitialized()) IotSdk.init(application)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(color = Color.Transparent) {
                    val viewModel: ProvisionViewModel = viewModel()
                    ProvisionScreen(
                        viewModel = viewModel,
                        onBack = { finish() },
                        onCompleted = { finish() },
                    )
                }
            }
        }
    }
}
