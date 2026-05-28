package com.yung.module_pdf.internal.data

import com.yung.module_pdf.api.PdfFileFormat
import com.yung.module_pdf.api.PdfRecentFile
import com.yung.module_pdf.db.FileInfoEntity
import com.yung.module_pdf.db.FileInfoFormat
import java.io.File

internal fun FileInfoFormat.toApi(): PdfFileFormat = when (this) {
    FileInfoFormat.PDF -> PdfFileFormat.PDF
    FileInfoFormat.Word -> PdfFileFormat.Word
    FileInfoFormat.Excel -> PdfFileFormat.Excel
    FileInfoFormat.PPT -> PdfFileFormat.PPT
    FileInfoFormat.Image -> PdfFileFormat.Image
}

internal fun PdfFileFormat.toEntity(): FileInfoFormat = when (this) {
    PdfFileFormat.PDF -> FileInfoFormat.PDF
    PdfFileFormat.Word -> FileInfoFormat.Word
    PdfFileFormat.Excel -> FileInfoFormat.Excel
    PdfFileFormat.PPT -> FileInfoFormat.PPT
    PdfFileFormat.Image -> FileInfoFormat.Image
}

internal fun FileInfoEntity.toPdfRecentFile(): PdfRecentFile = PdfRecentFile(
    id = id?.toLong() ?: 0L,
    name = name,
    path = path,
    size = size,
    lastOpenTime = time,
    format = format.toApi(),
)

internal fun PdfRecentFile.toEntity(): FileInfoEntity = FileInfoEntity(
    name = name,
    path = path,
    size = size,
    time = lastOpenTime,
    format = format.toEntity(),
    id = id.takeIf { it > 0L }?.toInt(),
)

internal fun File.toPdfRecentFile(format: FileInfoFormat): PdfRecentFile = PdfRecentFile(
    id = 0L,
    name = nameWithoutExtension,
    path = absolutePath,
    size = length(),
    lastOpenTime = System.currentTimeMillis(),
    format = format.toApi(),
)
