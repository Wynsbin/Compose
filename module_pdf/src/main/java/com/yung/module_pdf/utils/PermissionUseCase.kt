package com.yung.module_pdf.utils

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.fragment.app.FragmentActivity
import com.permissionx.guolindev.PermissionX

/**
 * 存储相关权限申请（基于 Guolin PermissionX）。
 *
 * - Android 11+：需「管理所有文件」([MANAGE_EXTERNAL_STORAGE])。
 * - Android 10（API 29）：扫描只需 [READ_EXTERNAL_STORAGE]；targetSdk 30+ 时 WRITE 对公共目录几乎无效。
 * - Android 9 及以下：READ + WRITE。
 */
object PermissionUseCase {

    private const val REASON_MESSAGE = "用于浏览和管理设备上的 PDF 文件，请授予存储相关权限。"
    private const val SETTINGS_MESSAGE_R =
        "请在系统设置中开启「允许管理所有文件」，否则无法扫描本地 PDF。"
    private const val SETTINGS_MESSAGE_LEGACY =
        "请在系统设置中开启存储读取权限，否则无法扫描本地 PDF。"
    private const val POSITIVE = "确定"
    private const val NEGATIVE = "取消"

    @JvmStatic
    fun useStorageManager(
        activity: FragmentActivity,
        allow: () -> Unit,
        denied: (() -> Unit)? = null,
    ) {
        if (hasStorageAccess(activity)) {
            allow()
            return
        }
        requestStoragePermissions(activity, onGranted = allow, onDenied = denied)
    }

    /** 是否具备扫描本地 PDF 列表所需的权限 */
    @JvmStatic
    fun hasStorageAccess(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager()
        }
        if (!PermissionX.isGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            return false
        }
        // API 28 及以下删除/重命名等仍依赖 WRITE
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
                PermissionX.isGranted(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    @JvmStatic
    fun requestStoragePermissions(
        activity: FragmentActivity,
        onGranted: () -> Unit,
        onDenied: (() -> Unit)? = null,
    ) {
        if (hasStorageAccess(activity)) {
            onGranted()
            return
        }
        val settingsMessage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            SETTINGS_MESSAGE_R
        } else {
            SETTINGS_MESSAGE_LEGACY
        }
        PermissionX.init(activity)
            .permissions(storagePermissions())
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(deniedList, REASON_MESSAGE, POSITIVE, NEGATIVE)
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(deniedList, settingsMessage, POSITIVE, NEGATIVE)
            }
            .request { allGranted, _, _ ->
                if (allGranted || hasStorageAccess(activity)) {
                    onGranted()
                } else {
                    onDenied?.invoke()
                }
            }
    }

    private fun storagePermissions(): List<String> = buildList {
        add(Manifest.permission.READ_EXTERNAL_STORAGE)
        add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
        }
    }
}
