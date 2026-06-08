package com.yung.anr.watchdog

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

internal object AnrWatchdogStore {

    private const val TAG = "AnrWatchdogStore"
    private const val REPORT_DIR = "anr_watchdog"
    private const val MAX_REPORTS = 20

    private val _reports = MutableStateFlow<List<AnrReport>>(emptyList())
    val reports: StateFlow<List<AnrReport>> = _reports.asStateFlow()

    private lateinit var reportDir: File

    fun init(context: Context) {
        reportDir = File(context.cacheDir, REPORT_DIR).apply { mkdirs() }
        _reports.value = reportDir.listFiles()
            ?.filter { it.isFile && it.extension == "txt" }
            ?.sortedByDescending { it.lastModified() }
            ?.take(MAX_REPORTS)
            ?.mapNotNull { file -> file.toReport() }
            .orEmpty()
        Log.d(TAG, "Loaded ${_reports.value.size} ANR report(s) from ${reportDir.absolutePath}")
    }

    fun saveReport(error: Error) {
        if (!::reportDir.isInitialized) return
        val timestamp = System.currentTimeMillis()
        val id = UUID.randomUUID().toString().take(8)
        val stackTrace = error.stackTraceToString()
        val fileName = "anr_${formatFileTime(timestamp)}_$id.txt"
        val file = File(reportDir, fileName)
        runCatching {
            file.writeText(buildReportContent(timestamp, error.message, stackTrace))
        }.onFailure {
            Log.e(TAG, "Failed to write ANR report", it)
        }
        val report = AnrReport(
            id = id,
            timestamp = timestamp,
            message = error.message.orEmpty(),
            stackTrace = stackTrace,
            filePath = file.takeIf { it.exists() }?.absolutePath,
        )
        _reports.update { current ->
            (listOf(report) + current).take(MAX_REPORTS)
        }
        Log.e(TAG, "ANR captured, saved to ${file.absolutePath}")
    }

    fun clearReports() {
        if (!::reportDir.isInitialized) return
        reportDir.listFiles()?.forEach { it.delete() }
        _reports.value = emptyList()
    }

    fun getReportDirPath(): String? =
        reportDir.takeIf { ::reportDir.isInitialized }?.absolutePath

    private fun File.toReport(): AnrReport? = runCatching {
        val content = readText()
        val timestamp = name.substringAfter("anr_").substringBefore("_").toLongOrNull()
            ?: lastModified()
        AnrReport(
            id = name.substringAfterLast("_").removeSuffix(".txt"),
            timestamp = timestamp,
            message = content.lineSequence().firstOrNull { it.startsWith("Message:") }
                ?.removePrefix("Message:")
                ?.trim()
                .orEmpty(),
            stackTrace = content.substringAfter("Stack trace:\n", content),
            filePath = absolutePath,
        )
    }.getOrNull()

    private fun buildReportContent(timestamp: Long, message: String?, stackTrace: String): String =
        buildString {
            appendLine("Time: ${formatDisplayTime(timestamp)}")
            appendLine("Message: ${message.orEmpty()}")
            appendLine()
            appendLine("Stack trace:")
            append(stackTrace)
        }

    private fun formatFileTime(timestamp: Long): String =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(timestamp))

    private fun formatDisplayTime(timestamp: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}
