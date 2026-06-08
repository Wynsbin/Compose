package com.yung.anr.watchdog

data class AnrReport(
    val id: String,
    val timestamp: Long,
    val message: String,
    val stackTrace: String,
    val filePath: String?,
)
