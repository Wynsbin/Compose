package com.yung.module_pdf.internal.core.permission

import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.fragment.app.FragmentActivity
import com.permissionx.guolindev.PermissionX

/**
 * 权限申请入口（Guolin PermissionX）。
 *
 * - [useStorageManager]：扫描与管理本地 PDF（Android 11+ 需「管理所有文件」）。
 * - [useGalleryImages]：从相册选图插入 PDF（Android 13+ 为 [PermissionCase.PER_READ_IMAGES]）。
 */
object PermissionUseCase {

    // region 存储 — 扫描 / 管理 PDF

    @JvmStatic
    fun useStorageManager(
        activity: FragmentActivity,
        allow: () -> Unit,
        denied: (() -> Unit)? = null,
    ) {
        runWithPermission(
            activity = activity,
            hasAccess = ::hasStorageAccess,
            permissions = PermissionCase.storageManagePermissions(),
            reasonMessage = MSG_STORAGE_REASON,
            settingsMessage = storageSettingsMessage(),
            onGranted = allow,
            onDenied = denied,
        )
    }

    @JvmStatic
    fun hasStorageAccess(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager()
        }
        if (!PermissionX.isGranted(context, PermissionCase.PER_STORAGE_READ)) {
            return false
        }
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
                PermissionX.isGranted(context, PermissionCase.PER_STORAGE_WRITE)
    }

    // endregion

    // region 相册 — 插入图片

    @JvmStatic
    fun useGalleryImages(
        activity: FragmentActivity,
        allow: () -> Unit,
        denied: (() -> Unit)? = null,
    ) {
        runWithPermission(
            activity = activity,
            hasAccess = ::hasGalleryImagesAccess,
            permissions = listOf(PermissionCase.galleryImagePermission()),
            reasonMessage = MSG_GALLERY_REASON,
            settingsMessage = MSG_GALLERY_SETTINGS,
            onGranted = allow,
            onDenied = denied,
        )
    }

    @JvmStatic
    fun hasGalleryImagesAccess(context: Context): Boolean =
        PermissionX.isGranted(context, PermissionCase.galleryImagePermission())

    // endregion

    // region 内部实现

    private const val MSG_STORAGE_REASON = "用于浏览和管理设备上的 PDF 文件，请授予存储相关权限。"
    private const val MSG_STORAGE_SETTINGS_R =
        "请在系统设置中开启「允许管理所有文件」，否则无法扫描本地 PDF。"
    private const val MSG_STORAGE_SETTINGS_LEGACY =
        "请在系统设置中开启存储读取权限，否则无法扫描本地 PDF。"
    private const val MSG_GALLERY_REASON = "用于从相册选择图片插入 PDF，请授予相册读取权限。"
    private const val MSG_GALLERY_SETTINGS = "请在系统设置中开启相册/存储读取权限，否则无法选择图片。"
    private const val BTN_POSITIVE = "确定"
    private const val BTN_NEGATIVE = "取消"

    private fun storageSettingsMessage(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            MSG_STORAGE_SETTINGS_R
        } else {
            MSG_STORAGE_SETTINGS_LEGACY
        }

    private fun runWithPermission(
        activity: FragmentActivity,
        hasAccess: (Context) -> Boolean,
        permissions: List<String>,
        reasonMessage: String,
        settingsMessage: String,
        onGranted: () -> Unit,
        onDenied: (() -> Unit)?,
    ) {
        if (hasAccess(activity)) {
            onGranted()
            return
        }
        requestPermissions(
            activity = activity,
            hasAccess = hasAccess,
            permissions = permissions,
            reasonMessage = reasonMessage,
            settingsMessage = settingsMessage,
            onGranted = onGranted,
            onDenied = onDenied,
        )
    }

    private fun requestPermissions(
        activity: FragmentActivity,
        hasAccess: (Context) -> Boolean,
        permissions: List<String>,
        reasonMessage: String,
        settingsMessage: String,
        onGranted: () -> Unit,
        onDenied: (() -> Unit)?,
    ) {
        if (hasAccess(activity)) {
            onGranted()
            return
        }
        PermissionX.init(activity)
            .permissions(permissions)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(deniedList, reasonMessage, BTN_POSITIVE, BTN_NEGATIVE)
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(deniedList, settingsMessage, BTN_POSITIVE, BTN_NEGATIVE)
            }
            .request { allGranted, _, _ ->
                if (allGranted || hasAccess(activity)) {
                    onGranted()
                } else {
                    onDenied?.invoke()
                }
            }
    }

    // endregion
}
