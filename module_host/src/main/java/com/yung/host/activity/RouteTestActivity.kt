package com.yung.host.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.yung.host.HostNavigator
import com.yung.host.theme.ComposeTheme

class RouteTestActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val activity = this
        setContent {
            ComposeTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                ) {
                    Button(onClick = { HostNavigator.toHome(activity) }) {
                        Text(text = "Open Home Module")
                    }
                    Button(onClick = { HostNavigator.toWeightEdit(activity) }) {
                        Text(text = "Open Home WeightEdit")
                    }
                    Button(onClick = { HostNavigator.toCategoryList(activity) }) {
                        Text(text = "Open Category List")
                    }
                    Button(onClick = { HostNavigator.toLogin(activity) }) {
                        Text(text = "Open User Login")
                    }
                    Button(onClick = { HostNavigator.toAbout(activity) }) {
                        Text(text = "Open About Us")
                    }
                }
            }
        }
    }
}
