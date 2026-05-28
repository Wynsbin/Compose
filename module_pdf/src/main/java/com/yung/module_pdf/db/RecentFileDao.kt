package com.yung.module_pdf.db

import android.net.Uri
import android.provider.MediaStore
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.yung.module_pdf.api.PdfSdk
import kotlinx.coroutines.flow.Flow
import java.io.File

@Dao
interface RecentFileDao {

    @Insert
    suspend fun insert(entity: FileInfoEntity)

    @Update
    suspend fun update(entity: FileInfoEntity)

    @Delete
    suspend fun delete(entity: FileInfoEntity)

    @Query("SELECT * FROM FileInfoEntity WHERE name = :name AND path = :path AND size = :size AND format = :format")
    suspend fun getByFile(
        name: String,
        path: String,
        size: Long,
        format: FileInfoFormat,
    ): FileInfoEntity?

    @Query("SELECT * FROM FileInfoEntity WHERE format=:oriFormat ORDER BY time DESC")
    fun getAll(oriFormat: FileInfoFormat): Flow<List<FileInfoEntity>>

    @Query("SELECT * FROM FileInfoEntity")
    fun getAll(): Flow<List<FileInfoEntity>>

    @Query("SELECT * FROM FileInfoEntity WHERE id=:id")
    fun getById(id: Int): Flow<FileInfoEntity>

    @Query("SELECT * FROM FileInfoEntity WHERE name LIKE '%' || :name || '%' ORDER BY time DESC")
    fun getLikeName(name: String): Flow<List<FileInfoEntity>>
}

suspend fun RecentFileDao.insertFile(path: String?, format: FileInfoFormat?) {
    path?.let {
        format?.let {
            val file = File(path)
            if (file.exists()) {
                insertEntity(
                    FileInfoEntity(
                        file.nameWithoutExtension,
                        file.path,
                        file.length(),
                        System.currentTimeMillis(),
                        format
                    )
                )
            }
        }
    }
}

suspend fun RecentFileDao.insertUri(uri: Uri?, format: FileInfoFormat?) {
    uri?.getPathFromUri()?.let {
        insertFile(it, format)
    }
}

suspend fun RecentFileDao.insertEntity(entity: FileInfoEntity) {
    //避免多次插入相同的数据文件
    val existing = getByFile(entity.name, entity.path, entity.size, entity.format)
    if (existing != null) {
        update(existing.copy(time = entity.time))
    } else {
        insert(entity)
    }
}

fun Uri.getPathFromUri(): String? {
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    PdfSdk.requireApp().contentResolver.query(this, projection, null, null, null)
        ?.use { cursor ->
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(columnIndex)
        }
    return null
}
