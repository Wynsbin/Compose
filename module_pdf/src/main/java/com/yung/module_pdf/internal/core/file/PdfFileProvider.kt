package com.yung.module_pdf.internal.core.file

import android.content.Context

/**
 * module_pdf SDK 内置 FileProvider 的 authority（方案 A：SDK 自带 Provider）。
 *
 * 合并进宿主 App 后为：`{applicationId}` + [AUTHORITY_SUFFIX]  
 * 例如 `com.example.app.module_pdf.fileProvider`。
 *
 * 集成方只需添加 SDK 依赖，无需在宿主 Manifest 中再声明 FileProvider。
 * 与宿主自带的 `.fileProvider`、其它 SDK 的 Provider 可共存。
 */
object PdfFileProvider {

    const val AUTHORITY_SUFFIX = ".module_pdf.fileProvider"

    fun authority(packageName: String): String = packageName + AUTHORITY_SUFFIX

    fun authority(context: Context): String = authority(context.packageName)
}
