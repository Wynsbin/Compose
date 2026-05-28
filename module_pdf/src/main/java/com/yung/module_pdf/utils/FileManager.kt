package com.yung.module_pdf.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.blankj.utilcode.util.ToastUtils
import java.io.File

object FileManager {

    private fun fileProviderAuthority(context: Context): String =
        PdfFileProvider.authority(context)

    /**
     * 将本地 [File] 转为可分享的 content [Uri]。
     * 优先走 FileProvider；若路径不在配置根目录内，则复制到应用 cache 再分享。
     */
    private fun getShareableUri(context: Context, file: File): Uri {
        val authority = fileProviderAuthority(context)
        return runCatching {
            FileProvider.getUriForFile(context, authority, file)
        }.getOrElse {
            copyToShareCache(context, file)
        }
    }

    private fun copyToShareCache(context: Context, source: File): Uri {
        val shareDir = File(context.cacheDir, "share").apply { mkdirs() }
        val target = File(shareDir, source.name)
        source.inputStream().use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        return FileProvider.getUriForFile(context, fileProviderAuthority(context), target)
    }

    private fun resolveMimeType(context: Context, file: File, uri: Uri): String {
        return context.contentResolver.getType(uri)
            ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension.lowercase())
            ?: "application/octet-stream"
    }

    private fun Intent.applyActivityFlags(context: Context): Intent = apply {
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (context !is Activity) {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun shareFile(context: Context, file: File) {
        if (!file.exists()) return ToastUtils.showShort("文件不存在")
        runCatching {
            val uri = getShareableUri(context, file)
            val mimeType = resolveMimeType(context, file, uri)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                applyActivityFlags(context)
            }
            context.startActivity(
                Intent.createChooser(shareIntent, file.nameWithoutExtension)
                    .applyActivityFlags(context)
            )
        }.onFailure {
            ToastUtils.showShort("分享失败")
        }
    }

    fun shareFile(context: Context, path: String) {
        shareFile(context, File(path))
    }

    fun openFile(context: Context, path: String) {
        val file = File(path)
        if (!file.exists()) return ToastUtils.showShort("文件不存在")
        runCatching {
            val uri = getShareableUri(context, file)
            val mimeType = resolveMimeType(context, file, uri)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                applyActivityFlags(context)
            }
            if (intent.resolveActivity(context.packageManager) == null) {
                return ToastUtils.showShort("没有找到可打开的应用")
            }
            context.startActivity(intent)
        }.onFailure {
            ToastUtils.showShort("打开失败")
        }
    }

    fun renameFile(path: String, newName: String, onCallBack: (File?, String) -> Unit) {
        val originalFile = File(path)
        val extension = originalFile.extension
        val newFile = File(originalFile.parent, "$newName.$extension")

        when {
            !originalFile.exists() -> onCallBack(null, "文件不存在")
            newFile.exists() -> onCallBack(null, "文件已存在")
            originalFile.renameTo(newFile) -> onCallBack(newFile, "重命名成功")
            else -> onCallBack(null, "重命名失败")
        }
    }

    fun deleteFile(path: String, onCallBack: (Boolean, String) -> Unit) {
        val targetFile = File(path)

        when {
            !targetFile.exists() -> onCallBack(false, "文件不存在")
            targetFile.isDirectory -> onCallBack(false, "这是目录不是文件")
            !targetFile.canWrite() -> onCallBack(false, "无写入权限")
            else -> {
                if (targetFile.delete()) {
                    onCallBack(true, "删除成功")
                } else {
                    onCallBack(false, "删除失败")
                }
            }
        }
    }
}
