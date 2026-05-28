package com.yung.module_pdf.api

import kotlinx.coroutines.flow.Flow

/**
 * 宿主自定义最近文件存储（可选）。
 * 配置后 SDK 不再写入内部 Room，改由宿主实现增删改查。
 */
interface RecentFileStore {

    fun observeAll(): Flow<List<PdfRecentFile>>

    fun observeByFormat(format: PdfFileFormat): Flow<List<PdfRecentFile>>

    suspend fun upsert(file: PdfRecentFile)

    suspend fun delete(id: Long)

    suspend fun deleteByPath(path: String, format: PdfFileFormat)
}
