package com.yung.module_pdf.api

import kotlinx.coroutines.flow.Flow

/** 对外暴露的最近文件只读/管理能力（不暴露 Room Entity / Dao）。 */
interface PdfRecentFileApi {

    fun observeAll(): Flow<List<PdfRecentFile>>

    fun observeByFormat(format: RecentFileFormat): Flow<List<PdfRecentFile>>

    suspend fun delete(id: Long)

    suspend fun clearAll()
}
