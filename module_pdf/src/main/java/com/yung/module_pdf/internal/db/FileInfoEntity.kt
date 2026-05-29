package com.yung.module_pdf.internal.db

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yung.module_pdf.api.RecentFileFormat
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class FileInfoEntity(
    val name: String,
    val path: String,
    var size: Long,
    val time: Long,
    val format: RecentFileFormat,
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
) : Parcelable
