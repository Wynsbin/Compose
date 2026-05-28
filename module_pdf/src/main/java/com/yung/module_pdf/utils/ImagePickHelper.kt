package com.yung.module_pdf.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File

object ImagePickHelper {

    fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        val cacheFile = copyUriToCache(context, uri) ?: return null
        return StickerImageUtils.fileToBitmap(cacheFile)
    }

    private fun copyUriToCache(context: Context, uri: Uri): File? = runCatching {
        val dir = File(context.filesDir, "image_pick")
        if (!dir.exists()) dir.mkdirs()
        val outFile = File(dir, "img_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            outFile.outputStream().use { output -> input.copyTo(output) }
        }
        outFile.takeIf { it.exists() && it.length() > 0L }
    }.getOrNull()
}
