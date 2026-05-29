package com.yung.compose.recent

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yung.module_pdf.api.RecentFileFormat

@Entity(tableName = "host_recent_file")
data class HostRecentFileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val path: String,
    val size: Long,
    val lastOpenTime: Long,
    val format: RecentFileFormat,
)
