package com.yung.module_pdf.db

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.io.File

@Parcelize
@Entity
data class FileInfoEntity(
    val name: String,
    val path: String,
    var size: Long,
    val time: Long,
    val format: FileInfoFormat,
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
) : Parcelable

enum class FileInfoFormat {
    PDF, Word, Excel, PPT, Image
}

fun getFileFormat(formatName: String): FileInfoFormat? = when (formatName) {
    FileInfoFormat.PDF.name -> FileInfoFormat.PDF
    FileInfoFormat.Word.name -> FileInfoFormat.Word
    FileInfoFormat.Excel.name -> FileInfoFormat.Excel
    FileInfoFormat.PPT.name -> FileInfoFormat.PPT
    FileInfoFormat.Image.name -> FileInfoFormat.Image
    else -> null
}

fun getFileFormat(file: File): FileInfoFormat? = when {
    file.name.endsWith(".pdf", ignoreCase = true) -> FileInfoFormat.PDF
    file.name.endsWith(".doc", ignoreCase = true) ||
            file.name.endsWith(".docx", ignoreCase = true) -> FileInfoFormat.Word

    file.name.endsWith(".xls", ignoreCase = true) ||
            file.name.endsWith(".xlsx", ignoreCase = true) ||
            file.name.endsWith(".csv", ignoreCase = true) -> FileInfoFormat.Excel

    file.name.endsWith(".ppt", ignoreCase = true) ||
            file.name.endsWith(".pptx", ignoreCase = true) -> FileInfoFormat.PPT

    else -> null
}


