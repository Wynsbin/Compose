package com.yung.module_pdf.internal.core.permission

import android.Manifest
import android.os.Build
import com.yung.module_pdf.R

object PermissionCase {

    // 存储
    const val PER_STORAGE_WRITE = Manifest.permission.WRITE_EXTERNAL_STORAGE
    const val PER_STORAGE_READ = Manifest.permission.READ_EXTERNAL_STORAGE

    // 外部存储
    const val PER_EXTERNAL_MANAGE = Manifest.permission.MANAGE_EXTERNAL_STORAGE

    // 媒体库，13及以上，图片视频获取
    const val PER_READ_IMAGES = Manifest.permission.READ_MEDIA_IMAGES
    const val PER_READ_VIDEO = Manifest.permission.READ_MEDIA_VIDEO
    const val PER_READ_AUDIO = Manifest.permission.READ_MEDIA_AUDIO

    // 相机
    const val PER_CAMERA = Manifest.permission.CAMERA

    // 录音、麦克风
    const val PER_RECORD = Manifest.permission.RECORD_AUDIO

    // 电话
    const val PER_PHONE_STATE = Manifest.permission.READ_PHONE_STATE

    // 位置
    const val PER_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    const val PER_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION

    // 日历
    const val PER_CALENDAR = Manifest.permission.READ_CALENDAR
    const val PER_WRITE_CALENDAR = Manifest.permission.WRITE_CALENDAR

    // 传感器
    const val PER_BODY_SENSOR = Manifest.permission.BODY_SENSORS
    const val PER_BACKGROUND_SENSOR = Manifest.permission.BODY_SENSORS_BACKGROUND

    /** API 33+ 读取相册图片；更低版本使用 [PER_STORAGE_READ]。 */
    fun galleryImagePermission(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PER_READ_IMAGES
        } else {
            PER_STORAGE_WRITE
        }

    /** 扫描 / 管理本地 PDF 时需申请的权限集合。 */
    fun storageManagePermissions(): List<String> = buildList {
        add(PER_STORAGE_READ)
        add(PER_STORAGE_WRITE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            add(PER_EXTERNAL_MANAGE)
        }
    }

    val permissionParis = listOf(
        PermissionParis(
            "存储权限",
            "用于实现文件保存到手机上，需要访问您的存储权限，如若拒绝，将无法使用上述功能。",
            R.drawable.icon_store,
            listOf(PER_STORAGE_WRITE, PER_STORAGE_READ),
            "存储权限：该功能需要存储权限才可以正常运行，用于访问存储空间内的媒体内容，诸如照片，音乐，视频等。"
        ),
        PermissionParis(
            "相机权限",
            "用于实现拍摄照片、录制视频进行创作，需要访问您的相机权限，如若拒绝，将无法使用上述功能。",
            R.drawable.icon_camera,
            listOf(PER_CAMERA),
            "相机权限：该功能需要相机权限才可以正常运行，用于拍摄照片或录制视频等"
        ),
        PermissionParis(
            "相册权限",
            "用于实现照片、视频制作，需要访问您的相册权限，如若拒绝，将无法使用上述功能。",
            R.drawable.icon_photoalbum,
            listOf(PER_READ_IMAGES, PER_READ_VIDEO),
            "相册权限：该功能需要相册权限才可以正常运行，用于照片、视频制作。"
        ),
        PermissionParis(
            "音频权限",
            "用于读取音乐、音频文件，需要访问您的音频权限，如若拒绝，将无法使用上述功能。",
            R.drawable.icon_photoalbum,
            listOf(PER_READ_AUDIO),
            "音频权限：该功能需要音频权限才可以正常运行，用于读取音乐、音频文件。"
        ),
        PermissionParis(
            "麦克风权限",
            "用于声音录取或语音识别,需要访问您的麦克风权限，如若拒绝，将无法使用上述功能。",
            R.drawable.icon_microphone,
            listOf(PER_RECORD),
            "麦克风权限：该功能需要麦克风权限才可以正常运行，用于声音录取或语音识别。"
        ),
        PermissionParis(
            "定位权限",
            "用于实现当前位置信息，需要访问您的定位权限，如若拒绝，将无法使用上述功能。",
            R.drawable.icon_orientation,
            listOf(PER_FINE_LOCATION, PER_COARSE_LOCATION),
            "定位权限：该功能需要定位权限才可以正常运行，用于获取您当前的位置信息。"
        ),
        PermissionParis(
            "电话设备权限",
            "用于访问通话状态信息等，需要访问您的电话设备权限，如若拒绝，将无法使用上述功能。",
            R.drawable.icon_telephone,
            listOf(PER_PHONE_STATE)
        ),
        PermissionParis(
            "外部存储权限",
            "用于实现文件管理（增、删、改、查），需要访问您的外部存储权限，如若拒绝，将无法使用上述功能。",
            R.drawable.icon_store,
            listOf(PER_EXTERNAL_MANAGE),
            "外部存储权限：该功能需要外部存储权限才可以正常运行，用于手机文件管理。"
        ),
        PermissionParis(
            "日历权限",
            "用于实现读写日历事件等，需要访问您的日历权限，如若拒绝，将无法使用上述功能。",
            R.drawable.icon_calendar,
            listOf(PER_CALENDAR, PER_WRITE_CALENDAR),
            "日历权限：该功能需要日历权限才可以正常运行，用于读写日历事件,进行日程管理。"
        ),
        PermissionParis(
            "传感器权限",
            "用于访问传感器的数据等，需要访问您的传感器权限，如若拒绝，将无法使用上述功能。",
            R.drawable.icon_sensor,
            listOf(PER_BODY_SENSOR, PER_BACKGROUND_SENSOR),
            "传感器权限：该功能需要传感器权限才可以正常运行，用于访问传感器的数据。"
        ),
    )
}
