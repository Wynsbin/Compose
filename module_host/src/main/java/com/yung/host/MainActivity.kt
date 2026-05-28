package com.yung.host

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import com.blankj.utilcode.util.ActivityUtils
import com.yung.host.theme.ComposeTheme
import com.yung.module_pdf.activity.PdfSelectActivity
import com.yung.module_pdf.common.PdfSelectMode

class MainActivity : FragmentActivity() {
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
                    Button(onClick = { HostNavigator.toCategoryList(activity) }) {
                        Text(text = "Open Category List")
                    }
                    Button(onClick = { HostNavigator.toLogin(activity) }) {
                        Text(text = "Open User Login")
                    }
                    Button(onClick = { HostNavigator.toAbout(activity) }) {
                        Text(text = "Open About Us")
                    }
                    Button(onClick = {
                        PdfSelectActivity.start(activity, PdfSelectMode.PREVIEW)
                    }) {
                        Text(text = "PDF")
                    }
                }
            }
        }
    }
}
