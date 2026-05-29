package com.yung.module_pdf.internal.data

import com.yung.module_pdf.api.RecentFileFormat
import com.yung.module_pdf.api.RecentFileStore
import com.yung.module_pdf.internal.db.FileInfoEntity
import com.yung.module_pdf.internal.db.RecentFileDb
import com.yung.module_pdf.internal.db.insertEntity
import com.yung.module_pdf.internal.db.insertFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

internal object RecentFileRepository {

    private var externalStore: RecentFileStore? = null

    fun init(store: RecentFileStore?) {
        externalStore = store
    }

    private fun dao() = RecentFileDb.getInstance().dao()

    fun observeByFormat(format: RecentFileFormat): Flow<List<FileInfoEntity>> {
        val store = externalStore
        return if (store != null) {
            store.observeByFormat(format).map { files -> files.map { it.toEntity() } }
        } else {
            dao().getAll(format)
        }
    }

    fun observeAll(): Flow<List<FileInfoEntity>> {
        val store = externalStore
        return if (store != null) {
            store.observeAll().map { files -> files.map { it.toEntity() } }
        } else {
            dao().getAll()
        }
    }

    suspend fun insertFile(path: String?, format: RecentFileFormat?) {
        if (path == null || format == null) return
        val file = File(path)
        if (!file.exists()) return
        val store = externalStore
        if (store != null) {
            store.upsert(file.toPdfRecentFile(format))
        } else {
            dao().insertFile(path, format)
        }
    }

    suspend fun insertEntity(entity: FileInfoEntity) {
        val store = externalStore
        if (store != null) {
            store.upsert(entity.toPdfRecentFile())
        } else {
            dao().insertEntity(entity)
        }
    }

    suspend fun update(entity: FileInfoEntity) {
        val store = externalStore
        if (store != null) {
            store.upsert(entity.toPdfRecentFile())
        } else {
            dao().update(entity)
        }
    }

    suspend fun delete(entity: FileInfoEntity) {
        val store = externalStore
        if (store != null) {
            val id = entity.id?.toLong()
            if (id != null) {
                store.delete(id)
            } else {
                store.deleteByPath(entity.path, entity.format)
            }
        } else {
            dao().delete(entity)
        }
    }

    suspend fun findByFile(
        name: String,
        path: String,
        size: Long,
        format: RecentFileFormat,
    ): FileInfoEntity? {
        if (externalStore != null) return null
        return dao().getByFile(name, path, size, format)
    }

    suspend fun deleteRecord(entity: FileInfoEntity) {
        if (entity.id != null) {
            delete(entity)
            return
        }
        val store = externalStore
        if (store != null) {
            store.deleteByPath(entity.path, entity.format)
        } else {
            findByFile(entity.name, entity.path, entity.size, entity.format)?.let { delete(it) }
        }
    }

    suspend fun updateRecord(entity: FileInfoEntity) {
        if (entity.id != null) {
            update(entity)
            return
        }
        val store = externalStore
        if (store != null) {
            store.upsert(entity.toPdfRecentFile())
        } else {
            findByFile(entity.name, entity.path, entity.size, entity.format)?.let { existing ->
                update(
                    existing.copy(
                        name = entity.name,
                        path = entity.path,
                        time = entity.time,
                    )
                )
            }
        }
    }
}
