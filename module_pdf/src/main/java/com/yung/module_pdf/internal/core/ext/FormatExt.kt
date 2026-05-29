package com.yung.module_pdf.internal.core.ext

import android.content.Intent
import android.os.Build
import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import com.yung.module_pdf.api.RecentFileFormat
import com.yung.module_pdf.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun formatFileSize(size: Long): String {
    if (size <= 0) return "0B"
    val units = arrayOf("TB", "GB", "MB", "KB", "B")
    val digitGroups = intArrayOf(0, 0, 0, 0, 0, 0)
    val tenKiB = 1024L // 1 KB
    val tenMiB = 1024L * 1024L // 1 MB
    val tenGiB = tenMiB * 1024L // 1 GB
    val tenTiB = tenGiB * 1024L // 1 TB

    when {
        size >= tenTiB -> digitGroups[0] = (size / tenTiB).toInt()
        size >= tenGiB -> digitGroups[1] = (size / tenGiB).toInt()
        size >= tenMiB -> digitGroups[2] = (size / tenMiB).toInt()
        size >= tenKiB -> digitGroups[3] = (size / tenKiB).toInt()
        else -> digitGroups[4] = size.toInt() // Bytes or KB
    }

    for (i in units.indices.reversed()) {
        if (digitGroups[i] > 0) {
            return "${digitGroups[i]}${units[i]}"
        }
    }
    return "0B"
}


fun formatFileTime(lastModified: Long): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = lastModified
    val lastModifiedDate = calendar.time
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(lastModifiedDate)
}

fun RecentFileFormat.getResId(): Int {
    return when (this) {
        RecentFileFormat.PDF -> R.mipmap.module_pdf_edit_icon_pdf
        RecentFileFormat.Word -> R.mipmap.module_pdf_edit_icon_word
        RecentFileFormat.Excel -> R.mipmap.module_pdf_edit_icon_excel
        RecentFileFormat.PPT -> R.mipmap.module_pdf_edit_icon_ppt
        RecentFileFormat.Image -> R.mipmap.module_pdf_edit_icon_tp
    }
}

fun <T : Parcelable> Intent.getCusParcelableExtra(name: String, clazz: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(name, clazz)
    } else {
        getParcelableExtra(name)
    }
}

fun <T : Parcelable> Intent.getCusParcelableArrayListExtra(
    name: String, clazz: Class<T>,
): ArrayList<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableArrayListExtra(name, clazz)
    } else {
        getParcelableArrayListExtra(name)
    }
}

fun Color.toAndroidColor(): Int {
    // 将 ComposeColor 的颜色通道值从 [0, 1] 范围转换到 [0, 255] 范围
    val red = (this.red * 255).toInt()
    val green = (this.green * 255).toInt()
    val blue = (this.blue * 255).toInt()
    val alpha = (this.alpha * 255).toInt()

    // 使用 AndroidColor.argb() 方法创建 AndroidColor 对象
    return android.graphics.Color.argb(alpha, red, green, blue)
}