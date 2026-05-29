package com.yung.compose.recent

import com.yung.module_pdf.api.PdfRecentFile
import com.yung.module_pdf.api.RecentFileFormat
import com.yung.module_pdf.api.RecentFileStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HostRecentFileStore(
    private val dao: HostRecentFileDao,
) : RecentFileStore {

    override fun observeAll(): Flow<List<PdfRecentFile>> =
        dao.observeAll().map { list -> list.map { it.toPdfRecentFile() } }

    override fun observeByFormat(format: RecentFileFormat): Flow<List<PdfRecentFile>> =
        dao.observeByFormat(format).map { list -> list.map { it.toPdfRecentFile() } }

    override suspend fun upsert(file: PdfRecentFile) {
        val existing = dao.findByUnique(
            name = file.name,
            path = file.path,
            size = file.size,
            format = file.format,
        )
        if (existing != null) {
            dao.update(
                existing.copy(
                    name = file.name,
                    lastOpenTime = file.lastOpenTime,
                )
            )
        } else {
            dao.insert(file.toHostEntity().copy(id = 0L))
        }
    }

    override suspend fun delete(id: Long) {
        dao.getById(id)?.let { dao.delete(it) }
    }

    override suspend fun deleteByPath(path: String, format: RecentFileFormat) {
        dao.deleteByPath(path, format)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}
