package com.yung.compose.recent

import com.yung.module_pdf.api.PdfRecentFile

fun HostRecentFileEntity.toPdfRecentFile(): PdfRecentFile = PdfRecentFile(
    id = id,
    name = name,
    path = path,
    size = size,
    lastOpenTime = lastOpenTime,
    format = format,
)

fun PdfRecentFile.toHostEntity(): HostRecentFileEntity = HostRecentFileEntity(
    id = id,
    name = name,
    path = path,
    size = size,
    lastOpenTime = lastOpenTime,
    format = format,
)
