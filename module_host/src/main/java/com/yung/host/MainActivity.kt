package com.yung.host

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.alibaba.android.arouter.launcher.ARouter
import com.yung.host.activity.PdfTestActivity
import com.yung.host.activity.RouteTestActivity
import com.yung.host.theme.ComposeTheme
import com.yung.route.RoutePath

class MainActivity : FragmentActivity() {

    private data class TestEntry(
        val title: String,
        val target: Class<*>? = null,
        val routePath: String? = null,
    ) {
        init {
            require((target != null) xor (routePath != null))
        }
    }

    private val entries = listOf(
        TestEntry("RouteTestActivity", target = RouteTestActivity::class.java),
        TestEntry("PdfTestActivity", target = PdfTestActivity::class.java),
        TestEntry("IoT Module", routePath = RoutePath.Iot.MAIN),
        TestEntry("ANR Module", routePath = RoutePath.Anr.MAIN),
        TestEntry("ANR Reports (Debug)", routePath = RoutePath.Anr.REPORTS),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeTheme {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(16.dp),
                ) {
                    item {
                        Text(
                            text = "Module Host Tests",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 16.dp),
                        )
                    }
                    items(entries) { entry ->
                        Text(
                            text = entry.title,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    when {
                                        entry.target != null ->
                                            startActivity(Intent(this@MainActivity, entry.target))
                                        entry.routePath != null ->
                                            ARouter.getInstance()
                                                .build(entry.routePath)
                                                .navigation(this@MainActivity)
                                    }
                                }
                                .padding(vertical = 16.dp),
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
