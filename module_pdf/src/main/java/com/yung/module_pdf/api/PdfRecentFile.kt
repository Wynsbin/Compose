package com.yung.module_pdf.api

data class PdfRecentFile(
    val id: Long,
    val name: String,
    val path: String,
    val size: Long,
    val lastOpenTime: Long,
    val format: RecentFileFormat,
)
