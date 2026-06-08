package com.yung.anr.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yung.anr.watchdog.AnrReport
import com.yung.anr.watchdog.AnrWatchdog
import com.yung.anr.watchdog.AnrWatchdogStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnrReportScreen() {
    val context = LocalContext.current
    val reports by AnrWatchdogStore.reports.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "ANR 采集记录") })
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
                WatchdogStatusCard(
                    reportCount = reports.size,
                    reportDir = AnrWatchdogStore.getReportDirPath(),
                    onClearReports = {
                        AnrWatchdogStore.clearReports()
                        Toast.makeText(context, "已清空 ANR 记录", Toast.LENGTH_SHORT).show()
                    },
                )
            }
            if (reports.isEmpty()) {
                item {
                    Text(
                        text = "暂无记录。触发 ANR 后堆栈会写入 cache/anr_watchdog，也可在 Logcat 过滤 AnrWatchdog。",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            } else {
                items(reports, key = { it.id }) { report ->
                    AnrReportCard(
                        report = report,
                        onCopy = { copyToClipboard(context, report.stackTrace) },
                    )
                }
            }
        }
    }
}

@Composable
private fun WatchdogStatusCard(
    reportCount: Int,
    reportDir: String?,
    onClearReports: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = if (AnrWatchdog.isRunning()) "ANR Watchdog 运行中" else "ANR Watchdog 未启动",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "超时阈值：${AnrWatchdog.DEFAULT_TIMEOUT_MS}ms | 已采集：$reportCount 条",
                style = MaterialTheme.typography.bodyMedium,
            )
            reportDir?.let { path ->
                Text(
                    text = "存储路径：$path",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            if (reportCount > 0) {
                OutlinedButton(onClick = onClearReports) {
                    Text(text = "清空记录")
                }
            }
        }
    }
}

@Composable
private fun AnrReportCard(
    report: AnrReport,
    onCopy: () -> Unit,
) {
    var expanded by remember(report.id) { mutableStateOf(false) }

    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = formatReportTime(report.timestamp),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = report.message.ifBlank { "主线程响应超时" },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (expanded) {
                Text(
                    text = report.stackTrace,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onCopy) {
                        Text(text = "复制堆栈")
                    }
                }
                report.filePath?.let { path ->
                    Text(
                        text = "文件：$path",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            } else {
                Text(
                    text = "点击展开堆栈",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}

private fun formatReportTime(timestamp: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    clipboard.setPrimaryClip(ClipData.newPlainText("anr_stack", text))
    Toast.makeText(context, "堆栈已复制", Toast.LENGTH_SHORT).show()
}
