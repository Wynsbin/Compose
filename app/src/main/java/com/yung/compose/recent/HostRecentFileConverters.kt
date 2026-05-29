package com.yung.compose.recent

import androidx.room.TypeConverter
import com.yung.module_pdf.api.RecentFileFormat

class HostRecentFileConverters {

    @TypeConverter
    fun fromFormat(value: RecentFileFormat): String = value.name

    @TypeConverter
    fun toFormat(value: String): RecentFileFormat = RecentFileFormat.valueOf(value)
}
