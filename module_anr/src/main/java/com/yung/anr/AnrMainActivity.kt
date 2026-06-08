package com.yung.anr

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.yung.anr.scenario.AnrScenario
import com.yung.anr.scenario.AnrScenarioExecutor
import com.yung.route.RoutePath

@Route(path = RoutePath.Anr.MAIN)
class AnrMainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnrTestScreen(
                onScenarioClick = { scenario ->
                    Toast.makeText(
                        this,
                        "已触发：${scenario.title}",
                        Toast.LENGTH_SHORT,
                    ).show()
                    AnrScenarioExecutor.trigger(this, scenario)
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnrTestScreen(
    onScenarioClick: (AnrScenario) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "ANR 测试用例") })
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "点击下方卡片触发 ANR 场景。堆栈由 Watchdog 静默采集，" +
                        "请到「ANR Reports」或 Logcat(AnrWatchdog) 查看。",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            items(AnrScenario.entries.toList()) { scenario ->
                AnrScenarioCard(
                    scenario = scenario,
                    onClick = { onScenarioClick(scenario) },
                )
            }
        }
    }
}

@Composable
private fun AnrScenarioCard(
    scenario: AnrScenario,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = scenario.title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = scenario.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "预期 ANR 类型：${scenario.anrType}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
