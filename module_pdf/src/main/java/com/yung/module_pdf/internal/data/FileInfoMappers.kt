package com.yung.module_pdf.internal.data

import com.yung.module_pdf.api.RecentFileFormat
import com.yung.module_pdf.api.PdfRecentFile
import com.yung.module_pdf.internal.db.FileInfoEntity
import java.io.File

internal fun FileInfoEntity.toPdfRecentFile(): PdfRecentFile = PdfRecentFile(
    id = id?.toLong() ?: 0L,
    name = name,
    path = path,
    size = size,
    lastOpenTime = time,
    format = format,
)

internal fun PdfRecentFile.toEntity(): FileInfoEntity = FileInfoEntity(
    name = name,
    path = path,
    size = size,
    time = lastOpenTime,
    format = format,
    id = id.takeIf { it > 0L }?.toInt(),
)

internal fun File.toPdfRecentFile(format: RecentFileFormat): PdfRecentFile = PdfRecentFile(
    id = 0L,
    name = nameWithoutExtension,
    path = absolutePath,
    size = length(),
    lastOpenTime = System.currentTimeMillis(),
    format = format,
)
