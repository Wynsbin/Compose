package com.yung.module_pdf.internal.core.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

/**
 * 用户添加图片贴纸，将所选图片转为bitmap
 */
object StickerImageUtils {
    /**
     * 将图片文件转换为Bitmap（自动处理OOM）
     * @param file 图片文件对象
     * @param maxSize 最大边长（像素），用于压缩
     */
    fun fileToBitmap(file: File, maxSize: Int = 200): Bitmap? {
        return try {
            // 第一步：只获取图片尺寸
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)

            // 第二步：计算采样率
            options.inSampleSize = calculateInSampleSize(options, maxSize, maxSize)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.RGB_565

            // 第三步：加载压缩后的Bitmap
            BitmapFactory.decodeFile(file.absolutePath, options)
        } catch (e: OutOfMemoryError) {
            System.gc()
            null
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height, width) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight &&
                halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}