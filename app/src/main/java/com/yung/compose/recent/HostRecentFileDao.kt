package com.yung.compose.recent

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yung.module_pdf.api.RecentFileFormat
import kotlinx.coroutines.flow.Flow

@Dao
interface HostRecentFileDao {

    @Query("SELECT * FROM host_recent_file ORDER BY lastOpenTime DESC")
    fun observeAll(): Flow<List<HostRecentFileEntity>>

    @Query("SELECT * FROM host_recent_file WHERE format = :format ORDER BY lastOpenTime DESC")
    fun observeByFormat(format: RecentFileFormat): Flow<List<HostRecentFileEntity>>

    @Query("SELECT * FROM host_recent_file WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): HostRecentFileEntity?

    @Query(
        """
        SELECT * FROM host_recent_file
        WHERE name = :name AND path = :path AND size = :size AND format = :format
        LIMIT 1
        """
    )
    suspend fun findByUnique(
        name: String,
        path: String,
        size: Long,
        format: RecentFileFormat,
    ): HostRecentFileEntity?

    @Query("DELETE FROM host_recent_file WHERE path = :path AND format = :format")
    suspend fun deleteByPath(path: String, format: RecentFileFormat)

    @Query("DELETE FROM host_recent_file")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HostRecentFileEntity): Long

    @Update
    suspend fun update(entity: HostRecentFileEntity)

    @Delete
    suspend fun delete(entity: HostRecentFileEntity)
}
