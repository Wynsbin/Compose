package com.yung.module_pdf.api

import java.io.File

enum class RecentFileFormat {
    PDF, Word, Excel, PPT, Image,
}

fun parseRecentFileFormat(formatName: String): RecentFileFormat? = when (formatName) {
    RecentFileFormat.PDF.name -> RecentFileFormat.PDF
    RecentFileFormat.Word.name -> RecentFileFormat.Word
    RecentFileFormat.Excel.name -> RecentFileFormat.Excel
    RecentFileFormat.PPT.name -> RecentFileFormat.PPT
    RecentFileFormat.Image.name -> RecentFileFormat.Image
    else -> null
}

fun resolveRecentFileFormat(file: File): RecentFileFormat? = when {
    file.name.endsWith(".pdf", ignoreCase = true) -> RecentFileFormat.PDF
    file.name.endsWith(".doc", ignoreCase = true) ||
            file.name.endsWith(".docx", ignoreCase = true) -> RecentFileFormat.Word

    file.name.endsWith(".xls", ignoreCase = true) ||
            file.name.endsWith(".xlsx", ignoreCase = true) ||
            file.name.endsWith(".csv", ignoreCase = true) -> RecentFileFormat.Excel

    file.name.endsWith(".ppt", ignoreCase = true) ||
            file.name.endsWith(".pptx", ignoreCase = true) -> RecentFileFormat.PPT

    else -> null
}
