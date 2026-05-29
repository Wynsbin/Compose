package com.yung.module_pdf.internal.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * ===============================================
 * Create by kongbaige on 2021/4/19
 * Email 1531603384@qq.com
 * ===============================================
 */
@Parcelize
class PdfInfoEntity(
    var path: String,
    var name: String,
    var size: Long,
    var time: Long,
) : Parcelable

