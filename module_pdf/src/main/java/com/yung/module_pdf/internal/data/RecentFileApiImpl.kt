package com.yung.module_pdf.internal.data

import com.yung.module_pdf.api.RecentFileFormat
import com.yung.module_pdf.api.PdfRecentFile
import com.yung.module_pdf.api.PdfRecentFileApi
import com.yung.module_pdf.api.PdfSdk
import com.yung.module_pdf.internal.db.RecentFileDb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

internal class RecentFileApiImpl : PdfRecentFileApi {

    override fun observeAll(): Flow<List<PdfRecentFile>> =
        RecentFileRepository.observeAll().map { list -> list.map { it.toPdfRecentFile() } }

    override fun observeByFormat(format: RecentFileFormat): Flow<List<PdfRecentFile>> =
        RecentFileRepository.observeByFormat(format)
            .map { list -> list.map { it.toPdfRecentFile() } }

    override suspend fun delete(id: Long) {
        val store = PdfSdk.config().recentFileStore
        if (store != null) {
            store.delete(id)
            return
        }
        val entity = RecentFileDb.getInstance().dao().getById(id.toInt()).first()
        RecentFileRepository.delete(entity)
    }

    override suspend fun clearAll() {
        val store = PdfSdk.config().recentFileStore
        if (store != null) return
        val dao = RecentFileDb.getInstance().dao()
        dao.getAll().first().forEach { RecentFileRepository.delete(it) }
    }
}
