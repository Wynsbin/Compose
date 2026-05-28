package com.yung.module_pdf.utils

import android.os.Environment
import com.yung.module_pdf.db.FileInfoEntity
import com.yung.module_pdf.db.FileInfoFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * 加载媒体文件的管理类
 */
object PdfLoadManager {

    private val localDocDirs = arrayOf(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
    )

    // 备用方案：直接扫描常见PDF目录
    fun scanAllPdf(): Flow<List<FileInfoEntity>> = flow {
        val pdfList = mutableListOf<FileInfoEntity>()
        localDocDirs.forEach { dir ->
            dir.walk().filter { it.isFile && it.extension.equals("pdf", true) }
                .forEach {
                    pdfList.add(
                        FileInfoEntity(
                            path = it.absolutePath,
                            name = it.nameWithoutExtension,
                            size = it.length(),
                            time = it.lastModified(),
                            format = FileInfoFormat.PDF
                        )
                    )
                }
        }
        emit(pdfList.sortedByDescending { it.time })
    }
}

