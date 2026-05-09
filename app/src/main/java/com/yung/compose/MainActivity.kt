package com.yung.compose

import android.R.attr.onClick
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import com.yung.host.HostNavigator
import com.yung.compose.ui.theme.ComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val activity = this
        setContent {
            ComposeTheme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Button(onClick = { HostNavigator.toHome(activity) }) {
                        Text(text = "Open Home Module")
                    }
                    Button(onClick = { HostNavigator.toWeightEdit(activity) }) {
                        Text(text = "Open Home WeightEdit")
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
